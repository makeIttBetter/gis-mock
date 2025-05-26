package com.example.realestate.service.upload;

import com.example.realestate.client.CensusGeocoderClient;
import com.example.realestate.client.utahmap.UtahMapServiceClient;
import com.example.realestate.converters.model.RealEstateUpdateConverter;
import com.example.realestate.converters.upload.CsvRowToRealEstateCsvRecordConverter;
import com.example.realestate.converters.upload.RealEstateCsvRecordToRealEstateConverter;
import com.example.realestate.dto.upload.RealEstateCsvErrorDto;
import com.example.realestate.dto.upload.RealEstateCsvRecord;
import com.example.realestate.dto.upload.RealEstateCsvUploadResult;
import com.example.realestate.dto.upload.RowData;
import com.example.realestate.model.RealEstate;
import com.example.realestate.repository.RealEstateRepository;
import com.example.realestate.util.UTMConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class RealEstateCsvService {

    private final RealEstateRepository realEstateRepository;
    private final CensusGeocoderClient censusGeocoderClient;
    private final UtahMapServiceClient utahMapServiceClient;
    private final RealEstateUpdateConverter realEstateUpdateConverter;
    private final RealEstateCsvRecordToRealEstateConverter recordToEntityConverter;

    @Value("${mapserv.utah.apikey}")
    private String mapservUtahApiKey;

    public RealEstateCsvService(RealEstateRepository realEstateRepository,
                                CensusGeocoderClient censusGeocoderClient,
                                UtahMapServiceClient utahMapServiceClient,
                                RealEstateUpdateConverter realEstateUpdateConverter,
                                RealEstateCsvRecordToRealEstateConverter recordToEntityConverter) {
        this.realEstateRepository = realEstateRepository;
        this.censusGeocoderClient = censusGeocoderClient;
        this.utahMapServiceClient = utahMapServiceClient;
        this.realEstateUpdateConverter = realEstateUpdateConverter;
        this.recordToEntityConverter = recordToEntityConverter;
    }

    /**
     * Legacy method (for convenience) if you don’t need partial progress in the Manager.
     * This creates a brand-new RealEstateCsvUploadResult inside.
     */
    public RealEstateCsvUploadResult processCsv(MultipartFile file) {
        RealEstateCsvUploadResult newResult = new RealEstateCsvUploadResult();
        return processCsv(file, newResult);
    }

    /**
     * Overloaded method: Takes in an existing RealEstateCsvUploadResult
     * so we can update processedRows in real-time.
     */
    public RealEstateCsvUploadResult processCsv(MultipartFile file, RealEstateCsvUploadResult result) {
        // Thread-safe lists for errors and unsaved rows:
        List<RealEstateCsvErrorDto> errorList = Collections.synchronizedList(new ArrayList<>());
        List<String[]> unsavedRows = Collections.synchronizedList(new ArrayList<>());

        // Use AtomicInteger for thread-safe counters:
        AtomicInteger processedRows = new AtomicInteger(0);
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger createdCount = new AtomicInteger(0);

        int totalRows;

        try (Reader reader = new InputStreamReader(file.getInputStream())) {

            // 1) Parse CSV and split rows into "missing lat/lon" vs. "with lat/lon"
            ParsedCsvData parsedData = parseAndSplitCsv(reader);
            totalRows = parsedData.totalRows;
            result.setHeaderRow(parsedData.headerRow);
            result.setTotalRows(totalRows);

            // 2) Process each group in parallel:
            //    - Single-thread for "missing lat/long"
            //    - Parallel for "with lat/long"

            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

            CompletableFuture<Void> missingLatLongFuture = CompletableFuture.runAsync(
                    () -> processRowsSequentially(
                            parsedData.rowsMissingLatLng,
                            errorList,
                            unsavedRows,
                            result, // pass the same result
                            processedRows,
                            updatedCount,
                            createdCount,
                            parsedData.headerMap
                    ),
                    singleThreadExecutor
            );

            CompletableFuture<Void> withLatLongFuture = CompletableFuture.runAsync(
                    () -> processRowsInParallel(
                            parsedData.rowsWithLatLng,
                            errorList,
                            unsavedRows,
                            result, // pass the same result
                            processedRows,
                            updatedCount,
                            createdCount,
                            parsedData.headerMap
                    )
            );

            CompletableFuture.allOf(missingLatLongFuture, withLatLongFuture).join();
            singleThreadExecutor.shutdown();

        } catch (Exception e) {
            log.error("Failed to process CSV file: {}", e.getMessage());
            RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
            errorDto.setRowNumber(0);
            errorDto.setErrorMessage("Failed to process CSV file: " + e.getMessage());
            errorList.add(errorDto);
            totalRows = 0;
        }

        // 3) Store final counters in the same 'result' object
        result.setProcessedRows(processedRows.get());
        result.setUpdatedCount(updatedCount.get());
        result.setCreatedCount(createdCount.get());
        result.setErrors(errorList);
        result.setUnsavedRows(unsavedRows);

        return result;
    }

    /* ========================================================================
       Parsing & Splitting
       ======================================================================== */

    private ParsedCsvData parseAndSplitCsv(Reader reader) throws Exception {
        CSVFormat format = CSVFormat.EXCEL.builder()
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .setIgnoreHeaderCase(false)
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        CSVParser csvParser = format.parse(reader);
        Map<String, Integer> apacheHeaderMap = csvParser.getHeaderMap();
        validateHeaderMap(apacheHeaderMap);

        String[] headerRow = buildHeaderRow(apacheHeaderMap);
        Map<String, Integer> lowerCaseHeaderMap = buildLowerCaseHeaderMap(headerRow);
        CsvRowToRealEstateCsvRecordConverter rowConverter =
                new CsvRowToRealEstateCsvRecordConverter(lowerCaseHeaderMap);

        List<RowData> rowsMissingLatLng = new ArrayList<>();
        List<RowData> rowsWithLatLng = new ArrayList<>();

        int totalRows = 0;
        for (CSVRecord record : csvParser) {
            totalRows++;
            String[] rowArray = convertRecordToArray(record, headerRow.length);

            try {
                RealEstateCsvRecord rec = rowConverter.convert(rowArray);
                RowData rowData = new RowData(rec, rowArray, totalRows);

                if (rec.getLatitude() == null || rec.getLongitude() == null) {
                    rowsMissingLatLng.add(rowData);
                } else {
                    rowsWithLatLng.add(rowData);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error converting row " + totalRows + ": " + ex.getMessage(), ex);
            }
        }

        ParsedCsvData parsed = new ParsedCsvData();
        parsed.totalRows = totalRows;
        parsed.headerRow = headerRow;
        parsed.headerMap = lowerCaseHeaderMap;
        parsed.rowsMissingLatLng = rowsMissingLatLng;
        parsed.rowsWithLatLng = rowsWithLatLng;
        return parsed;
    }

    private void validateHeaderMap(Map<String, Integer> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            throw new RuntimeException("No header row found in CSV. Please include a header row.");
        }
    }

    private String[] buildHeaderRow(Map<String, Integer> apacheHeaderMap) {
        String[] headerRow = new String[apacheHeaderMap.size()];
        apacheHeaderMap.forEach((colName, idx) -> headerRow[idx] = colName);
        return headerRow;
    }

    private String[] convertRecordToArray(CSVRecord record, int expectedLength) {
        String[] row = new String[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            row[i] = (i < record.size()) ? record.get(i) : null;
        }
        return row;
    }

    private Map<String, Integer> buildLowerCaseHeaderMap(String[] headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.length; i++) {
            String colName = (headerRow[i] == null ? "" : headerRow[i].trim().toLowerCase());
            map.put(colName, i);
        }
        return map;
    }

    /* ========================================================================
       Processing (single-thread vs. parallel)
       ======================================================================== */

    private void processRowsSequentially(
            List<RowData> rowDataList,
            List<RealEstateCsvErrorDto> errorList,
            List<String[]> unsavedRows,
            RealEstateCsvUploadResult partialResult, // pass the shared result
            AtomicInteger processedRows,
            AtomicInteger updatedCount,
            AtomicInteger createdCount,
            Map<String, Integer> headerMap
    ) {
        for (RowData rowData : rowDataList) {
            handleSingleRow(
                    rowData,
                    errorList,
                    unsavedRows,
                    partialResult,
                    processedRows,
                    updatedCount,
                    createdCount,
                    headerMap
            );
        }
    }

    private void processRowsInParallel(
            List<RowData> rowDataList,
            List<RealEstateCsvErrorDto> errorList,
            List<String[]> unsavedRows,
            RealEstateCsvUploadResult partialResult,
            AtomicInteger processedRows,
            AtomicInteger updatedCount,
            AtomicInteger createdCount,
            Map<String, Integer> headerMap
    ) {
        rowDataList.parallelStream().forEach(rowData ->
                handleSingleRow(
                        rowData,
                        errorList,
                        unsavedRows,
                        partialResult,
                        processedRows,
                        updatedCount,
                        createdCount,
                        headerMap
                )
        );
    }

    /**
     * Processes a single row: skipping if verified or missing MLS,
     * geocoding if needed, saving/updating in DB.
     * Always increments processedRows in a finally block.
     */
    private void handleSingleRow(
            RowData rowData,
            List<RealEstateCsvErrorDto> errorList,
            List<String[]> unsavedRows,
            RealEstateCsvUploadResult partialResult, // so we can update processedRows in real-time
            AtomicInteger processedRows,
            AtomicInteger updatedCount,
            AtomicInteger createdCount,
            Map<String, Integer> headerMap
    ) {
        if (Thread.currentThread().isInterrupted()) {
            log.warn("Aborting row processing for row #{}", rowData.getRowNumber());
            return;
        }

        String[] rowArray = rowData.getRowArray();
        RealEstateCsvRecord rec = rowData.getRecord();
        int rowNum = rowData.getRowNumber();

        normaliseAddressFields(rec); // Ensure address fields are normalised

        try {
            // 1) Skip if verified
            if (Boolean.TRUE.equals(rec.getVerified())) {
                skipRow(errorList, unsavedRows, rowArray, headerMap, rowNum,
                        rec.getMlsNumber(), "Verified=true, skipping row");
                return;
            }

            // 2) Skip if missing MLS#
            if (rec.getMlsNumber() == null || rec.getMlsNumber().isBlank()) {
                skipRow(errorList, unsavedRows, rowArray, headerMap, rowNum,
                        null, "Missing MLS#, skipping row");
                return;
            }

            // 3) If lat/lng missing => attempt geocoding
            if (rec.getLatitude() == null || rec.getLongitude() == null) {
                fillMissingLatLong(rec, rowArray, headerMap, rowNum, errorList, unsavedRows);
                if (rec.getLatitude() == null || rec.getLongitude() == null) {
                    return;
                }
            }

            // 4) Convert record -> entity
            RealEstate incomingEntity = recordToEntityConverter.convert(rec);

            // 5) Check if existing
            Optional<RealEstate> existingOpt = realEstateRepository.findByMlsNumber(rec.getMlsNumber());
            if (existingOpt.isPresent()) {
                // update existing
                RealEstate existing = existingOpt.get();
                realEstateUpdateConverter.updateFields(existing, incomingEntity);
                realEstateRepository.save(existing);
                updatedCount.incrementAndGet();
            } else {
                // create new
                realEstateRepository.save(incomingEntity);
                createdCount.incrementAndGet();
            }

        } catch (Exception ex) {
            log.error("Error processing row {}: {}", rowNum, ex.getMessage());
            addErrorAndRow(rowArray, headerMap, rowNum, ex.getMessage(), errorList, unsavedRows);
        } finally {
            // In all cases, increment processedRows
            int currentCount = processedRows.incrementAndGet();
            // Immediately update partialResult so UI sees real-time changes
            partialResult.setProcessedRows(currentCount);
        }
    }

    /* ========================================================================
       Geocoding
       ======================================================================== */

    private void fillMissingLatLong(
            RealEstateCsvRecord record,
            String[] row,
            Map<String, Integer> headerMap,
            int rowNum,
            List<RealEstateCsvErrorDto> errorList,
            List<String[]> unsavedRows
    ) {
        try {
            String address = buildFullAddress(record);
            boolean found = tryCensusGeocode(record, address) ||
                    tryUtahGeocode(record, address, record.getZip());
            if (!found) {
                skipRow(errorList, unsavedRows, row, headerMap, rowNum,
                        record.getMlsNumber(), "No lat/lng after geocoding");
            }
        } catch (Exception ex) {
            skipRow(errorList, unsavedRows, row, headerMap, rowNum,
                    record.getMlsNumber(), "Geocoding error: " + ex.getMessage());
        }
    }

    private String buildFullAddress(RealEstateCsvRecord rec) {
        String combined = String.format(
                "%s, %s, %s, %s",
                nullSafe(rec.getAddress()),
                nullSafe(rec.getCity()),
                nullSafe(rec.getState() == null || rec.getState().isEmpty()
                        ? "UT" : rec.getState().toUpperCase()),
                nullSafe(rec.getZip())
        ).replaceAll(", null", "").replaceAll("null,", "").trim();

        return (rec.getFullAddress() == null || rec.getFullAddress().isEmpty())
                ? combined
                : rec.getFullAddress().trim();
    }

    /**
     * Normalises address-related fields on a record **in-place**.
     * - Fills address/zip from fullAddress if missing.
     * - Builds fullAddress from pieces if it is blank.
     */
    private void normaliseAddressFields(RealEstateCsvRecord rec) {

        String city = Optional.ofNullable(rec.getCity()).orElse("").trim();
        String full = Optional.ofNullable(rec.getFullAddress()).orElse("").trim();
        String state = rec.getState() == null || rec.getState().isEmpty()
                ? "UT" : rec.getState().toUpperCase().trim();

        rec.setState(state); // Ensure state is always set
    /* -----------------------------------------------------------------
       A) fullAddress present  ➜  derive missing pieces
       ----------------------------------------------------------------- */
        if (!full.isEmpty()) {

            /* ZIP */
            if (rec.getZip() == null || rec.getZip().isBlank()) {
                // last ZIP-code looking chunk in the string (5 or 9 digits)
                Matcher m = Pattern.compile("\\b\\d{5}(?:-\\d{4})?\\b").matcher(full);
                String lastZip = null;
                while (m.find()) lastZip = m.group();          // keep last match
                if (lastZip != null) rec.setZip(lastZip);
            }

            /* Street address */
            if (rec.getAddress() == null || rec.getAddress().isBlank()) {
                int idx = full.toLowerCase().indexOf(city.toLowerCase());
                if (idx > 0) {
                    String beforeCity = full.substring(0, idx).replaceAll("[,\\s]+$", "");
                    if (!beforeCity.isBlank()) rec.setAddress(beforeCity);
                }
            }
        }

    /* -----------------------------------------------------------------
       B) fullAddress missing  ➜  build from parts
       ----------------------------------------------------------------- */
        if (rec.getFullAddress() == null || rec.getFullAddress().isBlank()) {
            String built = Stream.of(rec.getAddress(), city, rec.getState(), rec.getZip())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "));
            if (!built.isBlank()) rec.setFullAddress(built);
        }
    }

    private String nullSafe(String val) {
        return (val == null) ? "" : val;
    }

    private boolean tryCensusGeocode(RealEstateCsvRecord record, String addr) {
        try {
            var resp = censusGeocoderClient.geocode(addr, "4", "json");
            if (resp != null && resp.getResult() != null) {
                var matches = resp.getResult().getAddressMatches();
                if (matches != null && !matches.isEmpty()) {
                    var match = matches.get(0);
                    record.setLatitude(BigDecimal.valueOf(match.getCoordinates().getY()));
                    record.setLongitude(BigDecimal.valueOf(match.getCoordinates().getX()));
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("Census geocode failed for {}: {}", addr, e.getMessage());
        }
        return (record.getLatitude() != null && record.getLongitude() != null);
    }

    private boolean tryUtahGeocode(RealEstateCsvRecord record, String address, String zip) {
        try {
            var utahResp = utahMapServiceClient.geocode(address, zip, mapservUtahApiKey);
            if (utahResp != null && utahResp.getStatus() == 200 && utahResp.getResult() != null) {
                double x = utahResp.getResult().getLocation().getX();
                double y = utahResp.getResult().getLocation().getY();
                double[] latLon = UTMConverter.convertUTMToLatLon(x, y);
                record.setLatitude(BigDecimal.valueOf(latLon[0]));
                record.setLongitude(BigDecimal.valueOf(latLon[1]));
                return true;
            }
        } catch (Exception e) {
            log.warn("Utah geocode failed for {}, zip {}: {}", address, zip, e.getMessage());
        }
        return (record.getLatitude() != null && record.getLongitude() != null);
    }

    /* ========================================================================
       Error Handling
       ======================================================================== */

    private void skipRow(
            List<RealEstateCsvErrorDto> errorList,
            List<String[]> unsavedRows,
            String[] row,
            Map<String, Integer> headerMap,
            int rowNum,
            String mlsNumber,
            String reason
    ) {
        log.warn("Skipping row {}: {}", rowNum, reason);
        addErrorAndRow(row, headerMap, rowNum, reason, errorList, unsavedRows, mlsNumber);
    }

    private void addErrorAndRow(
            String[] row,
            Map<String, Integer> headerMap,
            int rowNum,
            String reason,
            List<RealEstateCsvErrorDto> errorList,
            List<String[]> unsavedRows
    ) {
        addErrorAndRow(row, headerMap, rowNum, reason, errorList, unsavedRows, null);
    }

    private void addErrorAndRow(
            String[] row,
            Map<String, Integer> headerMap,
            int rowNum,
            String reason,
            List<RealEstateCsvErrorDto> errorList,
            List<String[]> unsavedRows,
            String mlsNumber
    ) {
        RealEstateCsvErrorDto errorDto = buildErrorDto(row, headerMap, rowNum, reason);
        errorDto.setMlsNumber(mlsNumber);
        errorList.add(errorDto);
        unsavedRows.add(row);
    }

    private RealEstateCsvErrorDto buildErrorDto(
            String[] row,
            Map<String, Integer> headerMap,
            int rowIndex,
            String message
    ) {
        RealEstateCsvErrorDto dto = new RealEstateCsvErrorDto();
        dto.setRowNumber(rowIndex);

        String address = getCellValue(row, headerMap, "address");
        String city = getCellValue(row, headerMap, "city");
        String state = getCellValue(row, headerMap, "state");
        String zip = getCellValue(row, headerMap, "zip");

        String combinedAddr = String.join(", ",
                nullSafe(address), nullSafe(city),
                nullSafe(state), nullSafe(zip)
        );
        String fullAddress = getCellValue(row, headerMap, "full address");

        dto.setAddress((fullAddress != null && !fullAddress.isBlank())
                ? fullAddress
                : combinedAddr);
        dto.setErrorMessage(message);
        return dto;
    }

    private String getCellValue(String[] row, Map<String, Integer> headerMap, String columnName) {
        if (columnName == null) return null;
        Integer idx = headerMap.get(columnName.toLowerCase());
        if (idx == null || idx < 0 || idx >= row.length) {
            return null;
        }
        return row[idx];
    }

    /* ========================================================================
       Helper Classes
       ======================================================================== */

    private static class ParsedCsvData {
        int totalRows;
        String[] headerRow;
        Map<String, Integer> headerMap;
        List<RowData> rowsMissingLatLng;
        List<RowData> rowsWithLatLng;
    }
}

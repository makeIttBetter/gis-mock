package com.example.realestate.service.upload;

import com.example.realestate.client.CensusGeocoderClient;
import com.example.realestate.client.utahmap.UtahMapServiceClient;
import com.example.realestate.converters.model.RealEstateUpdateConverter;
import com.example.realestate.converters.upload.CsvRowToRealEstateCsvRecordConverter;
import com.example.realestate.converters.upload.RealEstateCsvRecordToRealEstateConverter;
import com.example.realestate.dto.upload.RealEstateCsvErrorDto;
import com.example.realestate.dto.upload.RealEstateCsvRecord;
import com.example.realestate.dto.upload.RealEstateCsvUploadResult;
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

    public RealEstateCsvUploadResult processCsv(MultipartFile file) {
        RealEstateCsvUploadResult result = new RealEstateCsvUploadResult();
        List<RealEstateCsvErrorDto> errorList = new ArrayList<>();
        List<String[]> unsavedRows = new ArrayList<>();

        int totalRows = 0;
        int processedRows = 0;
        int updatedCount = 0;
        int createdCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream())) {

            // 1) Build a "relaxed" CSVFormat to behave more like Excel.
            //    - EXCEL is typically more forgiving about unbalanced quotes.
            //    - You can also set withAllowMissingColumnNames(true), etc. if needed.
            CSVFormat format = CSVFormat.EXCEL.builder()
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setIgnoreHeaderCase(false)
                    .setHeader() // Let the first record be used as the header
                    .setSkipHeaderRecord(true) // so it won't appear as data
                    .build();


            // 2) Parse the file
            CSVParser csvParser = format.parse(reader);

            // 3) Extract the header
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            if (headerMap == null || headerMap.isEmpty()) {
                throw new RuntimeException("No header row found in CSV. Please include a header row.");
            }
            // We'll store the header names in an array for the result
            String[] headerRow = new String[headerMap.size()];
            headerMap.forEach((colName, idx) -> {
                // `colName` is the name in the header; `idx` is the column index
                // we must ensure we place them at the correct index in the array
                headerRow[idx] = colName;
            });
            result.setHeaderRow(headerRow);

            // Create a row->record converter using a "headerName -> index" map
            // We'll build our own map in lower-case keys to match your converter style
            Map<String, Integer> finalHeaderMap = buildLowerCaseHeaderMap(headerRow);

            CsvRowToRealEstateCsvRecordConverter rowConverter =
                    new CsvRowToRealEstateCsvRecordConverter(finalHeaderMap);

            // 4) For each CSV record (row) after the header
            for (CSVRecord record : csvParser) {
                totalRows++;

                // Build a raw String[] from the CSVRecord
                String[] rowArray = convertRecordToArray(record, headerRow.length);

                try {
                    // Convert to RealEstateCsvRecord
                    RealEstateCsvRecord rec = rowConverter.convert(rowArray);

                    // Possibly skip if verified, or skip if missing MLS#, etc.
                    if (Boolean.TRUE.equals(rec.getVerified())) {
                        skipRow(errorList, unsavedRows, rowArray, finalHeaderMap,
                                totalRows, rec.getMlsNumber(),
                                "Verified=true, skipping row");
                        continue;
                    }
                    if (rec.getMlsNumber() == null || rec.getMlsNumber().isBlank()) {
                        skipRow(errorList, unsavedRows, rowArray, finalHeaderMap,
                                totalRows, null, "Missing MLS#, skipping row");
                        continue;
                    }

                    // If lat/lng missing => attempt geocoding
                    if (rec.getLatitude() == null || rec.getLongitude() == null) {
                        fillMissingLatLong(rec, rowArray, finalHeaderMap,
                                totalRows, errorList, unsavedRows);
                        if (rec.getLatitude() == null || rec.getLongitude() == null) {
                            // still missing => skip
                            continue;
                        }
                    }

                    // Convert record -> entity
                    RealEstate incomingEntity = recordToEntityConverter.convert(rec);

                    Optional<RealEstate> existingOpt = realEstateRepository.findByMlsNumber(rec.getMlsNumber());
                    if (existingOpt.isPresent()) {
                        // update existing
                        RealEstate existing = existingOpt.get();
                        realEstateUpdateConverter.updateFields(existing, incomingEntity);
                        realEstateRepository.save(existing);
                        updatedCount++;
                    } else {
                        // create new
                        realEstateRepository.save(incomingEntity);
                        createdCount++;
                    }

                    processedRows++;

                } catch (Exception ex) {
                    log.error("Error processing row {}: {}", totalRows, ex.getMessage());
                    RealEstateCsvErrorDto errorDto = buildErrorDto(rowArray, finalHeaderMap, totalRows, ex.getMessage());
                    errorList.add(errorDto);
                    unsavedRows.add(rowArray);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process CSV file: {}", e.getMessage());
            RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
            errorDto.setRowNumber(0);
            errorDto.setErrorMessage("Failed to process CSV file: " + e.getMessage());
            errorList.add(errorDto);
        }

        // Summaries
        result.setTotalRows(totalRows);
        result.setProcessedRows(processedRows);
        result.setUpdatedCount(updatedCount);
        result.setCreatedCount(createdCount);
        result.setErrors(errorList);
        result.setUnsavedRows(unsavedRows);

        return result;
    }

    /**
     * Convert an Apache CSVRecord to a String[] so we can reuse existing logic.
     */
    private String[] convertRecordToArray(CSVRecord record, int expectedLength) {
        String[] row = new String[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            // If the CSV doesn't have a value for that column, fill it with null
            // or empty string. record.get(i) might throw if i is out of range,
            // so let's be safe.
            if (i < record.size()) {
                row[i] = record.get(i);
            } else {
                row[i] = null;
            }
        }
        return row;
    }

    /**
     * Build a lower-case columnName -> index map for your row converter to use.
     */
    private Map<String, Integer> buildLowerCaseHeaderMap(String[] headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.length; i++) {
            String colName = headerRow[i] == null ? "" : headerRow[i].trim().toLowerCase();
            map.put(colName, i);
        }
        return map;
    }

    /**
     * Attempt geocoding if lat/lng are missing, same logic as before.
     */
    private void fillMissingLatLong(RealEstateCsvRecord record,
                                    String[] row,
                                    Map<String, Integer> headerMap,
                                    int rowNum,
                                    List<RealEstateCsvErrorDto> errorList,
                                    List<String[]> unsavedRows) {
        try {
            String address = buildFullAddress(record);
            boolean found = tryCensusGeocode(record, address);
            if (!found) {
                found = tryUtahGeocode(record, address, record.getZip());
            }
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
        StringBuilder sb = new StringBuilder();
        if (rec.getAddress() != null) sb.append(rec.getAddress()).append(", ");
        if (rec.getCity() != null) sb.append(rec.getCity()).append(", ");
        if (rec.getState() != null) sb.append(rec.getState()).append(", ");
        if (rec.getZip() != null) sb.append(rec.getZip());

        String combinedAddr = sb.toString().replaceAll("null", "").trim();
        String fullAddress = rec.getFullAddress();
        return fullAddress == null || fullAddress.isEmpty()
                ? combinedAddr : fullAddress;
    }

    private boolean tryCensusGeocode(RealEstateCsvRecord record, String addr) {
        try {
            var resp = censusGeocoderClient.geocode(addr, "4", "json");
            if (resp != null && resp.getResult() != null &&
                    resp.getResult().getAddressMatches() != null &&
                    !resp.getResult().getAddressMatches().isEmpty()) {
                var match = resp.getResult().getAddressMatches().get(0);
                record.setLatitude(BigDecimal.valueOf(match.getCoordinates().getY()));
                record.setLongitude(BigDecimal.valueOf(match.getCoordinates().getX()));
                return true;
            }
        } catch (Exception e) {
            log.warn("Census geocode failed for {}: {}", addr, e.getMessage());
        }
        return record.getLatitude() != null && record.getLongitude() != null;
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
        return record.getLatitude() != null && record.getLongitude() != null;
    }

    private void skipRow(List<RealEstateCsvErrorDto> errorList,
                         List<String[]> unsavedRows,
                         String[] row,
                         Map<String, Integer> headerMap,
                         int rowNum,
                         String mlsNumber,
                         String reason) {
        log.warn("Skipping row {}: {}", rowNum, reason);

        RealEstateCsvErrorDto errorDto = buildErrorDto(row, headerMap, rowNum, reason);
        errorDto.setMlsNumber(mlsNumber);

        errorList.add(errorDto);
        unsavedRows.add(row);
    }

    private RealEstateCsvErrorDto buildErrorDto(String[] row,
                                                Map<String, Integer> headerMap,
                                                int rowIndex,
                                                String message) {
        var dto = new RealEstateCsvErrorDto();
        dto.setRowNumber(rowIndex);

        String address = getCellValue(row, headerMap, "address");
        String city = getCellValue(row, headerMap, "city");
        String state = getCellValue(row, headerMap, "state");
        String zip = getCellValue(row, headerMap, "zip");

        String combinedAddr = address + ", " + city + ", " + state + ", " + zip;
        String fullAddress = getCellValue(row, headerMap, "Full Address");
        System.out.println("ADDRESS: Full address: " + fullAddress);
        System.out.println("ADDRESS: Combined address: " + combinedAddr);
        dto.setAddress(fullAddress != null && !fullAddress.isEmpty()
                ? fullAddress : combinedAddr);

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
}

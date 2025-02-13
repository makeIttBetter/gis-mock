package com.example.realestate.service;

import com.example.realestate.client.CensusGeocoderClient;
import com.example.realestate.client.utahmap.UtahMapServiceClient;
import com.example.realestate.converters.model.RealEstateUpdateConverter;
import com.example.realestate.converters.upload.CsvRowToRealEstateCsvRecordConverter;
import com.example.realestate.converters.upload.RealEstateCsvRecordToRealEstateConverter;
import com.example.realestate.dto.api.CensusGeocodeResponse;
import com.example.realestate.dto.api.UtahGeocodeResponse;
import com.example.realestate.dto.upload.RealEstateCsvErrorDto;
import com.example.realestate.dto.upload.RealEstateCsvRecord;
import com.example.realestate.dto.upload.RealEstateCsvUploadResult;
import com.example.realestate.model.RealEstate;
import com.example.realestate.repository.RealEstateRepository;
import com.example.realestate.util.UTMConverter;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Process the uploaded CSV, converting each row to RealEstate and saving/updating.
     */
    public RealEstateCsvUploadResult processCsv(MultipartFile file) {
        RealEstateCsvUploadResult result = new RealEstateCsvUploadResult();
        List<RealEstateCsvErrorDto> errorList = new ArrayList<>();
        List<String[]> unsavedRows = new ArrayList<>();

        int totalRows = 0;
        int processedRows = 0;

        // Optionally track how many were updated vs. newly created
        int updatedCount = 0;
        int createdCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            // Use OpenCSV to read the entire file
            CSVParser csvParser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar(CSVParser.NULL_CHARACTER)
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(csvParser)
                    .build();

            List<String[]> allRows = csvReader.readAll();
            if (allRows.isEmpty()) {
                result.setTotalRows(0);
                result.setProcessedRows(0);
                result.setErrors(Collections.emptyList());
                result.setHeaderRow(null);
                result.setUnsavedRows(Collections.emptyList());
                return result;
            }

            // The first row is the header
            String[] header = allRows.get(0);
            result.setHeaderRow(header);

            // Build a map of columnName -> index from the header
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                headerMap.put(header[i].trim().toLowerCase(), i);
            }

            // Create our row -> record converter for this CSV, providing the headerMap
            CsvRowToRealEstateCsvRecordConverter csvRowConverter =
                    new CsvRowToRealEstateCsvRecordConverter(headerMap);

            // Now iterate over each data row
            for (int rowNum = 1; rowNum < allRows.size(); rowNum++) {
                totalRows++;
                String[] row = allRows.get(rowNum);

                try {
                    // Step 1: convert the raw row (String[]) to RealEstateCsvRecord
                    RealEstateCsvRecord record = csvRowConverter.convert(row);

                    // Implement your business logic rules:
                    // e.g. skip if Verified == true
                    if (Boolean.TRUE.equals(record.getVerified())) {
                        skipRow(errorList, unsavedRows, row, headerMap, rowNum, record.getMlsNumber(),
                                "Verified=true, skipping row");
                        continue;
                    }

                    // Must have MLS#
                    if (record.getMlsNumber() == null || record.getMlsNumber().isBlank()) {
                        skipRow(errorList, unsavedRows, row, headerMap, rowNum, null,
                                "Missing MLS#, skipping row");
                        continue;
                    }

                    // If lat/lng missing => attempt geocoding
                    if (record.getLatitude() == null || record.getLongitude() == null) {
                        fillMissingLatLong(record, row, headerMap, rowNum, errorList, unsavedRows);
                        if (record.getLatitude() == null || record.getLongitude() == null) {
                            // still missing => skip this row
                            continue;
                        }
                    }

                    // Step 2: convert RealEstateCsvRecord -> RealEstate entity
                    RealEstate incoming = recordToEntityConverter.convert(record);

                    // If there's an existing record by MLS#, we update
                    Optional<RealEstate> existingOpt =
                            realEstateRepository.findByMlsNumber(record.getMlsNumber());
                    if (existingOpt.isPresent()) {
                        RealEstate existing = existingOpt.get();
                        // Merge the new data
                        realEstateUpdateConverter.updateFields(existing, incoming);
                        realEstateRepository.save(existing);
                        updatedCount++;
                    } else {
                        // Create new
                        realEstateRepository.save(incoming);
                        createdCount++;
                    }
                    processedRows++;
                } catch (Exception ex) {
                    log.error("Error processing row {}: {}", rowNum + 1, ex.getMessage());
                    RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
                    errorDto.setRowNumber(rowNum + 1);
                    errorDto.setMlsNumber(getCellValue(row, headerMap, "mls#"));
                    String address = getCellValue(row, headerMap, "Address") +
                            ", " + getCellValue(row, headerMap, "City") +
                            ", " + getCellValue(row, headerMap, "State") +
                            ", " + getCellValue(row, headerMap, "Zip");
                    errorDto.setAddress(address);
                    errorDto.setErrorMessage("Processing error: " + ex.getMessage());
                    errorList.add(errorDto);

                    // Also add the entire row to unsaved
                    unsavedRows.add(row);
                }
            }

        } catch (Exception e) {
            // If something fails reading the file entirely
            log.error("Failed to process CSV file: {}", e.getMessage());
            RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
            errorDto.setRowNumber(0);
            errorDto.setErrorMessage("Failed to process CSV file: " + e.getMessage());
            errorList.add(errorDto);
        }

        // Fill in the summary
        result.setTotalRows(totalRows);
        result.setProcessedRows(processedRows);
        result.setErrors(errorList);
        result.setUnsavedRows(unsavedRows);

        // If you'd like to display these in the UI:
        result.setUpdatedCount(updatedCount);
        result.setCreatedCount(createdCount);
        return result;
    }

    /**
     * If lat/lng are missing, attempt geocoding. If it still fails, mark row as skipped.
     */
    private void fillMissingLatLong(RealEstateCsvRecord record,
                                    String[] row,
                                    Map<String, Integer> headerMap,
                                    int rowNum,
                                    List<RealEstateCsvErrorDto> errorList,
                                    List<String[]> unsavedRows) {
        try {
            String fullAddr = (record.getAddress() + ", " +
                    record.getCity() + ", " +
                    record.getState() + ", " +
                    record.getZip()).replaceAll("null", "").trim();
            String sanitized = sanitizeAddress(fullAddr);

            // Try Census first
            tryCensusGeocode(record, sanitized);

            // If still missing lat/lng, try Utah
            if (record.getLatitude() == null || record.getLongitude() == null) {
                tryUtahGeocode(record, sanitizeAddress(record.getAddress()), record.getZip());
            }

            // If STILL missing lat/lng, skip
            if (record.getLatitude() == null || record.getLongitude() == null) {
                skipRow(errorList, unsavedRows, row, headerMap, rowNum,
                        record.getMlsNumber(),
                        "No lat/lng after geocoding");
            }
        } catch (Exception ex) {
            skipRow(errorList, unsavedRows, row, headerMap, rowNum,
                    record.getMlsNumber(),
                    "Geocoding error: " + ex.getMessage());
        }
    }

    private void tryCensusGeocode(RealEstateCsvRecord record, String address) {
        try {
            CensusGeocodeResponse resp = censusGeocoderClient.geocode(address, "4", "json");
            if (resp != null && resp.getResult() != null &&
                    resp.getResult().getAddressMatches() != null &&
                    !resp.getResult().getAddressMatches().isEmpty()) {
                CensusGeocodeResponse.AddressMatch match = resp.getResult().getAddressMatches().get(0);
                record.setLatitude(BigDecimal.valueOf(match.getCoordinates().getY()));
                record.setLongitude(BigDecimal.valueOf(match.getCoordinates().getX()));
            }
        } catch (Exception e) {
            log.warn("Census geocode failed for {}: {}", address, e.getMessage());
        }
    }

    private void tryUtahGeocode(RealEstateCsvRecord record, String address, String zip) {
        try {
            UtahGeocodeResponse utahResp = utahMapServiceClient.geocode(address, zip, mapservUtahApiKey);
            if (utahResp != null && utahResp.getStatus() == 200) {
                double x = utahResp.getResult().getLocation().getX();
                double y = utahResp.getResult().getLocation().getY();
                double[] latLon = UTMConverter.convertUTMToLatLon(x, y);
                record.setLatitude(BigDecimal.valueOf(latLon[0]));
                record.setLongitude(BigDecimal.valueOf(latLon[1]));
            }
        } catch (Exception e) {
            log.warn("Utah geocode failed for {}, zip {}: {}", address, zip, e.getMessage());
        }
    }

    private String sanitizeAddress(String address) {
        if (address == null) return "";
        return address.replaceAll("[?#@]", "");
    }

    private void skipRow(List<RealEstateCsvErrorDto> errorList,
                         List<String[]> unsavedRows,
                         String[] row,
                         Map<String, Integer> headerMap,
                         int rowNum,
                         String mlsNumber,
                         String reason) {
        log.warn("Skipping row {}: {}", rowNum + 1, reason);
        RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
        errorDto.setRowNumber(rowNum + 1);
        errorDto.setMlsNumber(mlsNumber);
        String address = getCellValue(row, headerMap, "Address") +
                ", " + getCellValue(row, headerMap, "City") +
                ", " + getCellValue(row, headerMap, "State") +
                ", " + getCellValue(row, headerMap, "Zip");
        errorDto.setAddress(address);
        errorDto.setErrorMessage(reason);
        errorList.add(errorDto);

        unsavedRows.add(row);
    }

    private String getCellValue(String[] row, Map<String, Integer> headerMap, String columnName) {
        if (row == null || headerMap == null) return null;
        Integer index = headerMap.get(columnName.toLowerCase());
        if (index == null || index >= row.length) return null;
        return row[index].trim();
    }
}

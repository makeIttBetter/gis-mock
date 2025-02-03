package com.example.realestate.service;

import com.example.realestate.client.CensusGeocoderClient;
import com.example.realestate.client.UtahMapServiceClient;
import com.example.realestate.converters.CsvRowToRealEstateCsvRecordConverter;
import com.example.realestate.converters.RealEstateCsvRecordToRealEstateConverter;
import com.example.realestate.dto.CensusGeocodeResponse;
import com.example.realestate.dto.RealEstateCsvErrorDto;
import com.example.realestate.dto.RealEstateCsvRecord;
import com.example.realestate.dto.RealEstateCsvUploadResult;
import com.example.realestate.dto.UtahGeocodeResponse;
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
    private final RealEstateCsvRecordToRealEstateConverter recordToEntityConverter;

    private static int UTAH_MAPS_CALLS_COUNTER = 0;

    @Value("${mapserv.utah.apikey}")
    private String mapservUtahApiKey;

    public RealEstateCsvService(RealEstateRepository realEstateRepository,
                                CensusGeocoderClient censusGeocoderClient,
                                UtahMapServiceClient utahMapServiceClient,
                                RealEstateCsvRecordToRealEstateConverter recordToEntityConverter) {
        this.realEstateRepository = realEstateRepository;
        this.censusGeocoderClient = censusGeocoderClient;
        this.utahMapServiceClient = utahMapServiceClient;
        this.recordToEntityConverter = recordToEntityConverter;
    }

    /**
     * Processes the uploaded CSV file.
     * <p>
     * For each row, the CSV is converted into a RealEstateCsvRecord using
     * the CsvRowToRealEstateCsvRecordConverter.
     * If latitude and longitude are missing, the service first calls the Census Geocoder,
     * and then falls back to the Utah Gov Maps API if necessary.
     * Finally, the record is converted to a RealEstate entity and saved (unless its status is SOLD).
     * </p>
     * <p>
     * <b>Note:</b> If your CSV file has fields that contain newline characters (i.e. multi-line fields)
     * the default behavior of OpenCSV is to combine those lines into one record.
     * If you want every physical line to be treated as a record, we disable multi-line processing by
     * setting the quote character to a null value. (Make sure your CSV does not rely on quoting for other purposes!)
     * </p>
     *
     * @param file the uploaded CSV file
     * @return an upload result containing counts and error details
     */
    public RealEstateCsvUploadResult processCsv(MultipartFile file) {
        RealEstateCsvUploadResult result = new RealEstateCsvUploadResult();
        List<RealEstateCsvErrorDto> errorList = new ArrayList<>();
        int totalRows = 0;
        int processedRows = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            // Configure CSVParser to disable multi-line support.
            // This means that every physical line is treated as a record.
            CSVParser csvParser = new CSVParserBuilder()
                    .withSeparator(',')
                    // Setting the quote character to CSVParser.NULL_CHARACTER disables special handling of quotes.
                    .withQuoteChar(CSVParser.NULL_CHARACTER)
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(csvParser)
                    .build();

            // Read all rows from the CSV file.
            List<String[]> allRows = csvReader.readAll();

            // If there are no rows, return an empty result.
            if (allRows.isEmpty()) {
                result.setTotalRows(0);
                result.setProcessedRows(0);
                result.setErrors(Collections.emptyList());
                return result;
            }

            // Build header mapping: (column name in lower case -> index)
            String[] header = allRows.get(0);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                headerMap.put(header[i].trim().toLowerCase(), i);
            }

            // Create the CSV row converter with the header map.
            CsvRowToRealEstateCsvRecordConverter rowConverter = new CsvRowToRealEstateCsvRecordConverter(headerMap);

            // Process each row (starting after the header)
            log.info("Total physical lines (including header): {}", allRows.size());
            for (int rowNum = 1; rowNum < allRows.size(); rowNum++) {
                totalRows++;
                String[] row = allRows.get(rowNum);
                try {
                    // Convert CSV row to DTO using the converter.
                    RealEstateCsvRecord record = rowConverter.convert(row);

                    // If latitude/longitude are missing, try to obtain them.
                    if (record.getLatitude() == null || record.getLongitude() == null) {
                        // Build full address and sanitize it (remove reserved characters)
                        String fullAddr = record.getAddress() + ", " + record.getCity() + ", " + record.getState() + ", " + record.getZip();
                        String sanitizedAddress = sanitizeAddress(fullAddr);

                        try {
                            // First, try the Census Geocoder API.
                            CensusGeocodeResponse censusResponse = censusGeocoderClient.geocode(sanitizedAddress, "4", "json");
                            if (censusResponse != null &&
                                    censusResponse.getResult() != null &&
                                    censusResponse.getResult().getAddressMatches() != null &&
                                    !censusResponse.getResult().getAddressMatches().isEmpty()) {
                                // Use the first match (Census returns coordinates as x (lon) and y (lat))
                                CensusGeocodeResponse.AddressMatch match = censusResponse.getResult().getAddressMatches().get(0);
                                record.setLatitude(BigDecimal.valueOf(match.getCoordinates().getY()));
                                record.setLongitude(BigDecimal.valueOf(match.getCoordinates().getX()));
                            } else {
                                throw new Exception("Census geocoder returned no match");
                            }
                        } catch (Exception ex) {
                            log.warn("Census geocoder failed for address '{}': {}. Falling back to Utah Gov Maps API.",
                                    record.getAddress(), ex.getMessage());
                            // Fallback: call Utah Gov Maps API
                            try {
                                String geocodeAddress = sanitizeAddress(record.getAddress());
                                // Here, the actual API call is replaced with dummy data.
                                // TODO: Uncomment the real API call once ready.
                                 UtahGeocodeResponse utahResponse = utahMapServiceClient.geocode(geocodeAddress, record.getZip(), mapservUtahApiKey);
//                                UtahGeocodeResponse utahResponse = new UtahGeocodeResponse();
//                                UtahGeocodeResponse.Result resultt = new UtahGeocodeResponse.Result();
//                                UtahGeocodeResponse.Location location = new UtahGeocodeResponse.Location();
//
//                                location.setX(447480.4519172386);
//                                location.setY(4437423.661806457);
//                                resultt.setLocation(location);
//                                utahResponse.setResult(resultt);
//                                utahResponse.setStatus(200);

                                UTAH_MAPS_CALLS_COUNTER++;
                                if (utahResponse == null || utahResponse.getStatus() != 200) {
                                    throw new Exception("Utah Gov Maps geocoder failed");
                                }
                                double x = utahResponse.getResult().getLocation().getX();
                                double y = utahResponse.getResult().getLocation().getY();
                                // Convert UTM (x,y) to latitude/longitude
                                double[] latLon = UTMConverter.convertUTMToLatLon(x, y);
                                record.setLatitude(BigDecimal.valueOf(latLon[0]));
                                record.setLongitude(BigDecimal.valueOf(latLon[1]));
                            } catch (Exception innerEx) {
                                RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
                                errorDto.setRowNumber(rowNum + 1);
                                errorDto.setMlsNumber(record.getMlsNumber());
                                errorDto.setAddress(record.getAddress());
                                errorDto.setErrorMessage("Geocoding error (both Census and Utah): " + innerEx.getMessage().replaceAll("apiKey=[^&]+", ""));
                                errorList.add(errorDto);
                                continue;  // Skip this row
                            }
                        }
                    }

                    // Convert the DTO record to a RealEstate entity using the converter.
                    RealEstate realEstate = recordToEntityConverter.convert(record);

                    // Look up an existing record by MLS Number (which is unique).
                    Optional<RealEstate> existingOpt = realEstateRepository.findByMlsNumber(record.getMlsNumber());
                    if (existingOpt.isPresent()) {
                        if ("SOLD".equalsIgnoreCase(existingOpt.get().getStatus())) {
                            // Do not update records that are marked SOLD.
                            continue;
                        }
                        // For updates, preserve the existing ID so that the record is updated.
                        realEstate.setId(existingOpt.get().getId());
                    }
                    // Save the real estate record (new or updated).
                    realEstateRepository.save(realEstate);
                    processedRows++;
                } catch (Exception ex) {
                    log.error("Error processing row {}: {}", rowNum + 1, ex.getMessage());
                    RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
                    errorDto.setRowNumber(rowNum + 1);
                    errorDto.setMlsNumber(getValue(row, headerMap, "mls#"));
                    errorDto.setAddress(getValue(row, headerMap, "address"));
                    errorDto.setErrorMessage("Processing error: " + ex.getMessage());
                    errorList.add(errorDto);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process CSV file: {}", e.getMessage());
            RealEstateCsvErrorDto errorDto = new RealEstateCsvErrorDto();
            errorDto.setRowNumber(0);
            errorDto.setErrorMessage("Failed to process CSV file: " + e.getMessage());
            errorList.add(errorDto);
        }
        result.setTotalRows(totalRows);
        result.setProcessedRows(processedRows);
        result.setErrors(errorList);

        log.info("UTAH_MAPS_CALLS_COUNTER: {}", UTAH_MAPS_CALLS_COUNTER);
        return result;
    }

    /**
     * Sanitizes an address by removing reserved characters (such as ?, #, @, etc.)
     * that may cause the request URL to fail.
     *
     * @param address the input address string
     * @return a sanitized address string
     */
    private String sanitizeAddress(String address) {
        if (address == null) return null;
        return address.replaceAll("[\\?\\#\\@]", "");
    }

    /**
     * Helper: retrieves a column value by name from a raw CSV row using the header map.
     *
     * @param row        the CSV row
     * @param headerMap  mapping from header names to column indices
     * @param columnName the column name to retrieve
     * @return the trimmed value from the row or null if not present
     */
    private String getValue(String[] row, Map<String, Integer> headerMap, String columnName) {
        Integer index = headerMap.get(columnName.toLowerCase());
        if (index == null || index >= row.length) return null;
        return row[index].trim();
    }
}

package com.example.realestate.service.google;

import com.example.realestate.annotations.ExportField;
import com.example.realestate.export.cnst.RealEstateCsvConfig;
import com.example.realestate.model.Polygon;
import com.example.realestate.model.RealEstate;
import com.example.realestate.repository.PolygonRepository;
import com.example.realestate.service.PolygonService;
import com.example.realestate.service.RealEstateService;
import com.example.realestate.service.auth.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    private final SecurityService securityService;
    private final GoogleOAuth2Service googleOAuth2Service;
    private final PolygonRepository polygonRepository;
    private final PolygonService polygonService;
    private final RealEstateService realEstateService;
    private final RealEstateCsvConfig csvConfig;
    private final RestTemplate restTemplate;

    /**
     * Base URL for Google Sheets API, configured in application.properties.
     * Example:
     * google.sheets.api.baseUrl=https://sheets.googleapis.com/v4/spreadsheets
     */
    @Value("${google.sheets.api.baseUrl}")
    private String sheetsApiBaseUrl;

    public String exportPolygonDataToSheets(String polygonId) {
        String userId = securityService.getCurrentUserId();
        String accessToken = googleOAuth2Service.getValidAccessToken(userId);

        Polygon polygon = polygonRepository.findById(polygonId)
                .orElseThrow(() -> new RuntimeException("Polygon not found"));

        if (!polygon.getUserId().equals(userId)) {
            throw new RuntimeException("Polygon does not belong to the user");
        }

        List<RealEstate> realEstates = polygonService.findAttachedRealEstates(polygonId);

        String spreadsheetId = null;
        boolean isNew = false;
        String existingUrl = polygon.getGoogleSheetsUrl();
        if (existingUrl != null) {
            String existingId = extractSpreadsheetId(existingUrl);
            // Only check if we do have an existingId
            if (existingId != null && spreadsheetExists(accessToken, existingId)) {
                spreadsheetId = existingId;
            }
        }

        // If there's no existing spreadsheet, create a new one
        if (spreadsheetId == null) {
            spreadsheetId = createSpreadsheet(accessToken, polygon.getName());
            isNew = true;
        }

        // If spreadsheet already existed, clear the single sheet named "PolygonData"
        if (!isNew) {
            // FIX: always pass "PolygonData" instead of polygon.getName()
            clearSheet(accessToken, spreadsheetId, "PolygonData");
        }

        // Write data to the spreadsheet
        populateSpreadsheet(accessToken, spreadsheetId, realEstates);

        // Save the Google Sheets URL to the polygon
        String sheetsUrl = "https://docs.google.com/spreadsheets/d/" + spreadsheetId;
        polygon.setGoogleSheetsUrl(sheetsUrl);
        polygonRepository.save(polygon);

        return sheetsUrl;
    }

    public String exportMultiplePolygonsToSheets(List<String> polygonIds, String sheetName) {
        if (polygonIds == null || polygonIds.isEmpty()) {
            throw new IllegalArgumentException("At least one polygon ID must be provided");
        }
        if (sheetName == null || sheetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name must be provided");
        }

        String userId = securityService.getCurrentUserId();
        String accessToken = googleOAuth2Service.getValidAccessToken(userId);

        List<Polygon> polygons = polygonRepository.findAllByUserIdAndIdIn(userId, polygonIds);
        if (polygons.size() != polygonIds.size()) {
            throw new RuntimeException("Some polygons not found or do not belong to the user");
        }

        // Collect all RealEstate IDs across all polygons
        Set<String> allRealEstateIds = new HashSet<>();
        for (Polygon p : polygons) {
            List<String> reIds = polygonService.getRealEstateIdsForPolygon(p.getId());
            allRealEstateIds.addAll(reIds);
        }

        // Fetch them in bulk
        List<RealEstate> realEstates = realEstateService.getRealEstateEntitiesByIds(new ArrayList<>(allRealEstateIds));

        // Create a new spreadsheet with the provided sheetName as the spreadsheet's title
        String spreadsheetId = createSpreadsheet(accessToken, sheetName);

        // Populate the single sheet named "PolygonData"
        populateSpreadsheet(accessToken, spreadsheetId, realEstates);

        // Return final Sheets link
        return "https://docs.google.com/spreadsheets/d/" + spreadsheetId;
    }

    private String createSpreadsheet(String accessToken, String title) {
        // e.g., "https://sheets.googleapis.com/v4/spreadsheets"
        String url = sheetsApiBaseUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // Spreadsheet properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", title);

        // Single sheet named "PolygonData"
        Map<String, Object> spreadsheet = new HashMap<>();
        spreadsheet.put("properties", properties);

        List<Map<String, Object>> sheets = new ArrayList<>();
        Map<String, Object> sheet = new HashMap<>();
        Map<String, Object> sheetProperties = new HashMap<>();
        sheetProperties.put("title", "PolygonData");
        sheet.put("properties", sheetProperties);
        sheets.add(sheet);

        spreadsheet.put("sheets", sheets);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(spreadsheet, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to create spreadsheet");
        }

        Map<?, ?> json = response.getBody();
        String spreadsheetId = (String) json.get("spreadsheetId");
        log.info("Created spreadsheet with ID: {}", spreadsheetId);
        return spreadsheetId;
    }

    private void populateSpreadsheet(String accessToken, String spreadsheetId, List<RealEstate> realEstates) {
        List<List<String>> sheetData = prepareSheetData(realEstates);
        int numRows = sheetData.size();
        int numCols = sheetData.isEmpty() ? 0 : sheetData.get(0).size();

        // For example, "PolygonData!A1:D25"
        String range = "PolygonData!A1:" + getColumnLetter(numCols) + numRows;
        log.info("Updating range: {}", range);

        // e.g. "https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/PolygonData!A1:D25?valueInputOption=RAW"
        String url = sheetsApiBaseUrl + "/" + spreadsheetId
                + "/values/" + range + "?valueInputOption=RAW";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("values", sheetData);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to populate spreadsheet");
        }
    }

    private List<List<String>> prepareSheetData(List<RealEstate> realEstates) {
        Map<String, AnnotatedAccessor> annotatedAccessors = new HashMap<>();
        scanClassHierarchy(RealEstate.class, annotatedAccessors);

        // The config has the order of columns to export
        List<String> configKeysInOrder = new ArrayList<>(csvConfig.getExportColumnsOrdered().keySet());

        // Create the header row from the config's display names
        List<String> headerRow = configKeysInOrder.stream()
                .map(key -> csvConfig.getExportColumnsOrdered().get(key))
                .collect(Collectors.toList());

        // Build data rows
        List<List<String>> dataRows = new ArrayList<>();
        for (RealEstate re : realEstates) {
            List<String> row = new ArrayList<>();
            for (String fieldName : configKeysInOrder) {
                AnnotatedAccessor accessor = annotatedAccessors.get(fieldName);
                String cellValue = "";
                if (accessor != null) {
                    try {
                        Object val = accessor.getValue(re);
                        cellValue = (val == null) ? "" : val.toString();
                    } catch (Exception e) {
                        log.warn("Cannot read field {}: {}", fieldName, e.getMessage());
                    }
                }
                row.add(cellValue);
            }
            dataRows.add(row);
        }

        // Final result: first row = header, then data rows
        List<List<String>> sheetData = new ArrayList<>();
        sheetData.add(headerRow);
        sheetData.addAll(dataRows);

        return sheetData;
    }

    private void scanClassHierarchy(Class<?> clazz, Map<String, AnnotatedAccessor> annotatedAccessors) {
        if (clazz == null || clazz.equals(Object.class)) {
            return;
        }

        for (Field field : clazz.getDeclaredFields()) {
            ExportField ann = field.getAnnotation(ExportField.class);
            if (ann != null) {
                field.setAccessible(true);
                annotatedAccessors.put(ann.fieldName(), new FieldAccessor(field));
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            ExportField ann = method.getAnnotation(ExportField.class);
            if (ann != null) {
                method.setAccessible(true);
                annotatedAccessors.put(ann.fieldName(), new MethodAccessor(method));
            }
        }

        scanClassHierarchy(clazz.getSuperclass(), annotatedAccessors);
    }

    /**
     * Checks if a spreadsheet exists by calling GET on its ID.
     */
    private boolean spreadsheetExists(String accessToken, String spreadsheetId) {
        if (spreadsheetId == null) {
            return false;
        }
        // e.g. "https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}"
        String url = sheetsApiBaseUrl + "/" + spreadsheetId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return (response.getStatusCode() == HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw ex;
        } catch (Exception e) {
            log.error("Error checking spreadsheet existence: {}", e.getMessage());
            throw new RuntimeException("Failed to check spreadsheet existence", e);
        }
    }

    private void clearSheet(String accessToken, String spreadsheetId, String sheetName) {
        // e.g. "https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/{sheetName}:clear"
        String url = sheetsApiBaseUrl + "/" + spreadsheetId + "/values/" + sheetName + ":clear";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to clear sheet: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error clearing sheet: {}", e.getMessage());
            throw new RuntimeException("Failed to clear sheet", e);
        }
    }

    private String extractSpreadsheetId(String url) {
        if (url == null) {
            return null;
        }
        // For a share link: https://docs.google.com/spreadsheets/d/{spreadsheetId}/edit...
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals("d")) {
                return parts[i + 1];
            }
        }
        return null;
    }

    private String getColumnLetter(int colIndex) {
        StringBuilder sb = new StringBuilder();
        while (colIndex > 0) {
            colIndex--;
            sb.insert(0, (char) ('A' + (colIndex % 26)));
            colIndex /= 26;
        }
        return sb.toString();
    }

    // Accessors for fields/methods annotated with @ExportField
    private interface AnnotatedAccessor {
        Object getValue(Object instance) throws Exception;
    }

    private static class FieldAccessor implements AnnotatedAccessor {
        private final Field field;

        public FieldAccessor(Field field) {
            this.field = field;
        }

        @Override
        public Object getValue(Object instance) throws Exception {
            return field.get(instance);
        }
    }

    private static class MethodAccessor implements AnnotatedAccessor {
        private final Method method;

        public MethodAccessor(Method method) {
            this.method = method;
        }

        @Override
        public Object getValue(Object instance) throws Exception {
            return method.invoke(instance);
        }
    }
}

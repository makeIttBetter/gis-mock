package com.example.realestate.service.google;

import com.example.realestate.annotations.ExportDataType;
import com.example.realestate.annotations.ExportField;
import com.example.realestate.export.cnst.RealEstateCsvConfig;
import com.example.realestate.export.service.RealEstateExportValueFormatter;
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
    private final RealEstateExportValueFormatter valueFormatter;
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
        Map<String, AnnotatedAccessor> annotatedAccessors = buildAnnotatedAccessors();
        List<String> configKeysInOrder = new ArrayList<>(csvConfig.getExportColumnsOrdered().keySet());
        List<List<Object>> sheetData = prepareSheetData(realEstates, annotatedAccessors, configKeysInOrder);
        int numRows = sheetData.size();
        int numCols = sheetData.isEmpty() ? 0 : sheetData.get(0).size();

        // For example, "PolygonData!A1:D25"
        String range = "PolygonData!A1:" + getColumnLetter(numCols) + numRows;
        log.info("Updating range: {}", range);

        // e.g. "https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/PolygonData!A1:D25?valueInputOption=USER_ENTERED"
        String url = sheetsApiBaseUrl + "/" + spreadsheetId
                + "/values/" + range + "?valueInputOption=USER_ENTERED";

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

        applyColumnFormats(accessToken, spreadsheetId, configKeysInOrder, annotatedAccessors, numRows);
    }

    private Map<String, AnnotatedAccessor> buildAnnotatedAccessors() {
        Map<String, AnnotatedAccessor> annotatedAccessors = new HashMap<>();
        scanClassHierarchy(RealEstate.class, annotatedAccessors);
        return annotatedAccessors;
    }

    private List<List<Object>> prepareSheetData(List<RealEstate> realEstates,
                                                Map<String, AnnotatedAccessor> annotatedAccessors,
                                                List<String> configKeysInOrder) {
        List<Object> headerRow = new ArrayList<>();
        for (String key : configKeysInOrder) {
            headerRow.add(csvConfig.getExportColumnsOrdered().get(key));
        }

        List<List<Object>> dataRows = new ArrayList<>();
        for (RealEstate re : realEstates) {
            List<Object> row = new ArrayList<>();
            for (String fieldName : configKeysInOrder) {
                AnnotatedAccessor accessor = annotatedAccessors.get(fieldName);
                Object cellValue = "";
                if (accessor != null) {
                    try {
                        Object val = accessor.getValue(re);
                        ExportDataType exportType = accessor.getExportType();
                        RealEstateExportValueFormatter.FormatResult formatted =
                                valueFormatter.formatValue(fieldName, exportType, val);
                        Object sheetValue = formatted.sheetValue();
                        cellValue = (sheetValue == null) ? "" : sheetValue;
                    } catch (Exception e) {
                        log.warn("Cannot read field {}: {}", fieldName, e.getMessage());
                    }
                }
                row.add(cellValue);
            }
            dataRows.add(row);
        }

        List<List<Object>> sheetData = new ArrayList<>();
        sheetData.add(headerRow);
        sheetData.addAll(dataRows);

        return sheetData;
    }

    private void applyColumnFormats(String accessToken,
                                    String spreadsheetId,
                                    List<String> configKeysInOrder,
                                    Map<String, AnnotatedAccessor> annotatedAccessors,
                                    int numRows) {
        Integer sheetId = fetchSheetId(accessToken, spreadsheetId, "PolygonData");
        if (sheetId == null) {
            log.warn("Unable to resolve sheetId for PolygonData; skipping column formatting");
            return;
        }

        int effectiveRowCount = Math.max(numRows, 1);

        List<Map<String, Object>> requests = new ArrayList<>();
        for (int colIndex = 0; colIndex < configKeysInOrder.size(); colIndex++) {
            String fieldName = configKeysInOrder.get(colIndex);
            AnnotatedAccessor accessor = annotatedAccessors.get(fieldName);
            ExportDataType exportType = (accessor != null) ? accessor.getExportType() : ExportDataType.STRING;
            Map<String, Object> numberFormat = buildNumberFormat(exportType);
            if (numberFormat == null) {
                continue;
            }

            Map<String, Object> userEnteredFormat = new HashMap<>();
            userEnteredFormat.put("numberFormat", numberFormat);

            Map<String, Object> cell = new HashMap<>();
            cell.put("userEnteredFormat", userEnteredFormat);

            Map<String, Object> range = new HashMap<>();
            range.put("sheetId", sheetId);
            range.put("startRowIndex", 0);
            range.put("endRowIndex", effectiveRowCount);
            range.put("startColumnIndex", colIndex);
            range.put("endColumnIndex", colIndex + 1);

            Map<String, Object> repeatCell = new HashMap<>();
            repeatCell.put("range", range);
            repeatCell.put("cell", cell);
            repeatCell.put("fields", "userEnteredFormat.numberFormat");

            Map<String, Object> request = new HashMap<>();
            request.put("repeatCell", repeatCell);
            requests.add(request);
        }

        if (requests.isEmpty()) {
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("requests", requests);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        String url = sheetsApiBaseUrl + "/" + spreadsheetId + ":batchUpdate";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed to apply column formats: {}", response.getStatusCode());
        }
    }

    private Map<String, Object> buildNumberFormat(ExportDataType exportType) {
        Map<String, Object> numberFormat = new HashMap<>();
        switch (exportType) {
            case STRING -> {
                numberFormat.put("type", "TEXT");
                numberFormat.put("pattern", "@");
            }
            case INTEGER -> {
                numberFormat.put("type", "NUMBER");
                numberFormat.put("pattern", "0");
            }
            case DECIMAL -> {
                numberFormat.put("type", "NUMBER");
                numberFormat.put("pattern", "#,##0.########");
            }
            case DATE -> {
                numberFormat.put("type", "DATE");
                numberFormat.put("pattern", "mm/dd/yyyy");
            }
            case DATETIME -> {
                numberFormat.put("type", "DATE_TIME");
                numberFormat.put("pattern", "mm/dd/yyyy hh:mm:ss");
            }
            case CURRENCY -> {
                numberFormat.put("type", "CURRENCY");
                numberFormat.put("pattern", "$#,##0");
            }
            default -> {
                return null;
            }
        }
        return numberFormat;
    }

    private Integer fetchSheetId(String accessToken, String spreadsheetId, String sheetName) {
        String url = sheetsApiBaseUrl + "/" + spreadsheetId + "?fields=sheets(properties(sheetId,title))";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            Object sheetsObj = response.getBody().get("sheets");
            if (!(sheetsObj instanceof List<?> sheets)) {
                return null;
            }
            for (Object sheetObj : sheets) {
                if (!(sheetObj instanceof Map<?, ?> sheet)) {
                    continue;
                }
                Object propsObj = sheet.get("properties");
                if (!(propsObj instanceof Map<?, ?> props)) {
                    continue;
                }
                Object titleObj = props.get("title");
                if (sheetName.equals(titleObj)) {
                    Object sheetIdObj = props.get("sheetId");
                    if (sheetIdObj instanceof Number number) {
                        return number.intValue();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Unable to fetch sheetId for {}: {}", sheetName, ex.getMessage());
        }
        return null;
    }

    private void scanClassHierarchy(Class<?> clazz, Map<String, AnnotatedAccessor> annotatedAccessors) {
        if (clazz == null || clazz.equals(Object.class)) {
            return;
        }

        for (Field field : clazz.getDeclaredFields()) {
            ExportField ann = field.getAnnotation(ExportField.class);
            if (ann != null) {
                field.setAccessible(true);
                annotatedAccessors.put(ann.fieldName(), new FieldAccessor(field, ann.exportType()));
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            ExportField ann = method.getAnnotation(ExportField.class);
            if (ann != null) {
                method.setAccessible(true);
                annotatedAccessors.put(ann.fieldName(), new MethodAccessor(method, ann.exportType()));
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

        ExportDataType getExportType();
    }

    private static class FieldAccessor implements AnnotatedAccessor {
        private final Field field;
        private final ExportDataType exportType;

        public FieldAccessor(Field field, ExportDataType exportType) {
            this.field = field;
            this.exportType = exportType;
        }

        @Override
        public Object getValue(Object instance) throws Exception {
            return field.get(instance);
        }

        @Override
        public ExportDataType getExportType() {
            return exportType;
        }
    }

    private static class MethodAccessor implements AnnotatedAccessor {
        private final Method method;
        private final ExportDataType exportType;

        public MethodAccessor(Method method, ExportDataType exportType) {
            this.method = method;
            this.exportType = exportType;
        }

        @Override
        public Object getValue(Object instance) throws Exception {
            return method.invoke(instance);
        }

        @Override
        public ExportDataType getExportType() {
            return exportType;
        }
    }
}

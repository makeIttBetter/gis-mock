package com.example.realestate.export.service;

import com.example.realestate.annotations.ExportDataType;
import com.example.realestate.annotations.ExportField;
import com.example.realestate.dto.model.PolygonDto;
import com.example.realestate.export.cnst.RealEstateCsvConfig;
import com.example.realestate.model.RealEstate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that streams RealEstate data to CSV using reflection.
 * It scans both fields and methods (in the entire class hierarchy)
 * for @ExportField(fieldName=...) to build a map from "fieldName" -> accessor.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealEstateCsvExportService {

    private final RealEstateCsvConfig csvConfig;
    private final RealEstateExportValueFormatter valueFormatter;

    /**
     * Streams a CSV file with all columns defined in RealEstateCsvConfig,
     * looking up values via @ExportField annotated fields *or* methods,
     * including those on superclasses (like Model).
     */
    public void exportToCsv(List<RealEstate> realEstateList,
                            PolygonDto polygon,
                            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        String filename = polygon.getName() + ".csv";
        log.info("exportToCsv() called with filename: {}", filename);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        // 1) Build a "fieldName -> AnnotatedAccessor" map by scanning RealEstate + superclasses.
        Map<String, AnnotatedAccessor> annotatedAccessors = new HashMap<>();
        scanClassHierarchy(RealEstate.class, annotatedAccessors);

        // 2) Build our header row from RealEstateCsvConfig in the exact order:
        List<String> headerRow = new ArrayList<>();
        // We'll keep the config keys in a list, in order, so we can iterate them for each row.
        List<String> configKeysInOrder = new ArrayList<>();

        for (Map.Entry<String, String> entry : csvConfig.getExportColumnsOrdered().entrySet()) {
            String fieldName = entry.getKey();    // e.g. "days_back"
            String header = entry.getValue();     // e.g. "Days Back"
            headerRow.add(header);
            configKeysInOrder.add(fieldName);
        }

        try (PrintWriter writer = response.getWriter()) {
            // (a) Write the header line
            writer.println(String.join(",", headerRow));

            // (b) For each RealEstate, fill columns in config order
            for (RealEstate re : realEstateList) {
                List<String> rowValues = new ArrayList<>();

                for (String fieldName : configKeysInOrder) {
                    AnnotatedAccessor accessor = annotatedAccessors.get(fieldName);
                    if (accessor == null) {
                        // If no matching field/method => empty column
                        rowValues.add("");
                        continue;
                    }
                    try {
                        Object val = accessor.getValue(re);
                        ExportDataType exportType = accessor.getExportType();
                        RealEstateExportValueFormatter.FormatResult formatted =
                                valueFormatter.formatValue(fieldName, exportType, val);
                        String cellValue = escapeCsv(formatted.csvValue());
                        rowValues.add(cellValue);
                    } catch (Exception e) {
                        log.warn("Cannot read field {}: {}", fieldName, e.getMessage());
                        rowValues.add("");
                    }
                }
                writer.println(String.join(",", rowValues));
            }
            writer.flush();
        }
    }

    /**
     * Recursively scan a class + its superclasses for:
     * - Fields with @ExportField
     * - Methods with @ExportField
     * Put them in annotatedAccessors map (key = fieldName, value = accessor).
     */
    private void scanClassHierarchy(Class<?> clazz, Map<String, AnnotatedAccessor> annotatedAccessors) {
        if (clazz == null || clazz.equals(Object.class)) {
            return;
        }

        // 1) Fields
        for (Field field : clazz.getDeclaredFields()) {
            ExportField ann = field.getAnnotation(ExportField.class);
            if (ann != null) {
                field.setAccessible(true);
                annotatedAccessors.put(ann.fieldName(), new FieldAccessor(field, ann.exportType()));
            }
        }

        // 2) Methods
        for (Method method : clazz.getDeclaredMethods()) {
            ExportField ann = method.getAnnotation(ExportField.class);
            if (ann != null) {
                method.setAccessible(true);
                annotatedAccessors.put(ann.fieldName(), new MethodAccessor(method, ann.exportType()));
            }
        }

        // Recurse up the superclass
        scanClassHierarchy(clazz.getSuperclass(), annotatedAccessors);
    }

    /**
     * CSV escaping: If a cell has comma, quotes, or newlines,
     * wrap in quotes and double any existing quotes.
     */
    private String escapeCsv(String input) {
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            input = input.replace("\"", "\"\"");
            return "\"" + input + "\"";
        }
        return input;
    }

    // -----------------------------------------------------------------
    // Accessor interfaces
    // -----------------------------------------------------------------

    /**
     * An interface that can return a value from an object (RealEstate),
     * either by reading a field or calling a method.
     */
    private interface AnnotatedAccessor {
        Object getValue(Object instance) throws Exception;

        ExportDataType getExportType();
    }

    /**
     * Accessor for an annotated field.
     */
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

    /**
     * Accessor for an annotated method.
     */
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

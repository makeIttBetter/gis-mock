package com.example.realestate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark fields or getter methods for export.
 * - fieldName: the unique name of the field (should match the export order constant).
 * - displayName: the header to display in the export file.
 * - exportType: the data type Excel/Sheets should treat the column as.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ExportField {
    String fieldName();

    String displayName();

    ExportDataType exportType() default ExportDataType.STRING;
}

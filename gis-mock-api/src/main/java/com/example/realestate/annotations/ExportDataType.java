package com.example.realestate.annotations;

/**
 * Enumerates the supported export data types so CSV/Google Sheets
 * can format the values correctly.
 */
public enum ExportDataType {
    STRING,
    INTEGER,
    DECIMAL,
    DATE,
    DATETIME,
    CURRENCY
}

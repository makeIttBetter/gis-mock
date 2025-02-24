package com.example.realestate.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Central helper to parse strings into various data types,
 * handling quotes, commas, special placeholders, etc.
 */
@Slf4j
public final class CsvParsingUtils {

    private CsvParsingUtils() {
        // Utility class; no instances allowed.
    }

    /**
     * Remove surrounding quotes, if present.
     */
    public static String stripSurroundingQuotes(String raw) {
        if (raw == null) return null;
        String val = raw.trim();
        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
            val = val.substring(1, val.length() - 1).trim();
        }
        return val;
    }

    /**
     * Attempts to parse an integer, ignoring non‐digit characters (commas, etc.).
     */
    public static Integer parseInteger(String value) {
        if (value == null) return null;
        String cleaned = value.replaceAll("[^\\d\\-]", ""); // keep digits + possible minus sign
        if (cleaned.isEmpty()) return null;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            log.debug("parseInteger failed for '{}'", value);
            return null;
        }
    }

    /**
     * Attempts to parse a decimal, removing $ signs, commas, etc.
     */
    public static BigDecimal parseBigDecimal(String value) {
        if (value == null) return null;
        String cleaned = value.replaceAll("[$,]", "").trim();
        if (cleaned.isEmpty()) return null;
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            log.debug("parseBigDecimal failed for '{}'", value);
            return null;
        }
    }

    /**
     * Attempts to parse multiple date patterns, ignoring "00/00/0000" or empty strings as null.
     */
    public static LocalDate parseDate(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        // Treat "00/00/0000" or blank as null
        if (trimmed.equals("00/00/0000") || trimmed.isEmpty()) {
            return null;
        }

        // We might have either 2‐digit or 4‐digit year. Let's try multiple patterns:
        String[] patterns = {
            "M/d/uuuu",    // e.g. 9/19/2024
            "M/d/yy",      // e.g. 9/19/24
            "MM/dd/uuuu",  // e.g. 09/19/2024
            "uuuu-MM-dd"   // fallback if the string is ISO format
        };

        for (String p : patterns) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(p);
                return LocalDate.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        // If we fail all patterns, log and return null
        log.debug("parseDate failed for '{}', no matching patterns", value);
        return null;
    }

    /**
     * Attempts to parse a "yes/true/1" or "no/false/0".
     */
    public static Boolean parseBoolean(String value) {
        if (value == null) return null;
        String lower = value.trim().toLowerCase();
        if (lower.equals("yes") || lower.equals("true") || lower.equals("1")) {
            return true;
        }
        if (lower.equals("no") || lower.equals("false") || lower.equals("0")) {
            return false;
        }
        // Otherwise unknown
        return null;
    }

    /**
     * Attempt to parse a price using NumberFormat if needed,
     * but typically parseBigDecimal is enough if you store it as BigDecimal.
     */
    public static double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0.0;
        }
        // Remove $ and commas, then parse
        String cleaned = priceStr.replaceAll("[$,]", "");
        try {
            Number number = NumberFormat.getInstance(Locale.US).parse(cleaned);
            return number.doubleValue();
        } catch (Exception e) {
            log.debug("parsePrice failed for '{}'", priceStr);
            return 0.0;
        }
    }
}

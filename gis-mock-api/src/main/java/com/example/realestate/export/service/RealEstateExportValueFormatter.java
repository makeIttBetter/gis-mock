package com.example.realestate.export.service;

import com.example.realestate.annotations.ExportDataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formats exported RealEstate values so that CSV rows and Google Sheets rows
 * honor the data type attached to the @ExportField annotation.
 */
@Component
@Slf4j
public class RealEstateExportValueFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    public FormatResult formatValue(String fieldName, ExportDataType exportType, Object rawValue) {
        if (rawValue == null) {
            return FormatResult.empty();
        }

        return switch (exportType) {
            case INTEGER -> formatInteger(rawValue);
            case DECIMAL -> formatDecimal(rawValue);
            case DATE -> formatDate(rawValue);
            case DATETIME -> formatDateTime(rawValue);
            case CURRENCY -> formatCurrency(rawValue);
            case STRING -> formatString(rawValue);
        };
    }

    private FormatResult formatString(Object rawValue) {
        if (rawValue instanceof Boolean boolValue) {
            String csvValue = Boolean.TRUE.equals(boolValue) ? "true" : "false";
            return new FormatResult(csvValue, csvValue);
        }
        String sanitized = sanitizeString(rawValue.toString());
        if (sanitized == null) {
            return FormatResult.empty();
        }
        return new FormatResult(sanitized, sanitized);
    }

    private FormatResult formatInteger(Object rawValue) {
        Long numericValue = null;
        if (rawValue instanceof Number number) {
            numericValue = number.longValue();
        } else {
            String sanitized = sanitizeString(rawValue.toString());
            numericValue = parseLong(sanitized);
            if (numericValue == null && sanitized != null) {
                return new FormatResult(sanitized, sanitized);
            }
        }

        if (numericValue == null) {
            return FormatResult.empty();
        }

        String csvValue = Long.toString(numericValue);
        return new FormatResult(csvValue, numericValue);
    }

    private FormatResult formatDecimal(Object rawValue) {
        BigDecimal numericValue = toBigDecimal(rawValue);
        if (numericValue == null) {
            String sanitized = sanitizeString(rawValue.toString());
            if (sanitized == null) {
                return FormatResult.empty();
            }
            return new FormatResult(sanitized, sanitized);
        }
        BigDecimal normalized = numericValue.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }
        String csvValue = normalized.toPlainString();
        return new FormatResult(csvValue, normalized);
    }

    private FormatResult formatDate(Object rawValue) {
        LocalDate date = null;
        if (rawValue instanceof LocalDate localDate) {
            date = localDate;
        } else if (rawValue instanceof LocalDateTime localDateTime) {
            date = localDateTime.toLocalDate();
        } else if (rawValue instanceof CharSequence) {
            date = parseToLocalDate(sanitizeString(rawValue.toString()));
        }

        if (date == null) {
            String sanitized = sanitizeString(rawValue.toString());
            if (sanitized == null) {
                return FormatResult.empty();
            }
            return new FormatResult(sanitized, sanitized);
        }

        String formatted = DATE_FORMATTER.format(date);
        return new FormatResult(formatted, formatted);
    }

    private FormatResult formatDateTime(Object rawValue) {
        LocalDateTime dateTime = null;
        if (rawValue instanceof LocalDateTime ldt) {
            dateTime = ldt;
        } else if (rawValue instanceof LocalDate localDate) {
            dateTime = localDate.atStartOfDay();
        } else if (rawValue instanceof CharSequence) {
            dateTime = parseToLocalDateTime(sanitizeString(rawValue.toString()));
        }

        if (dateTime == null) {
            String sanitized = sanitizeString(rawValue.toString());
            if (sanitized == null) {
                return FormatResult.empty();
            }
            return new FormatResult(sanitized, sanitized);
        }

        String formatted = DATE_TIME_FORMATTER.format(dateTime);
        return new FormatResult(formatted, formatted);
    }

    private FormatResult formatCurrency(Object rawValue) {
        BigDecimal numericValue = toBigDecimal(rawValue);
        if (numericValue == null) {
            String sanitized = sanitizeString(rawValue.toString());
            if (sanitized == null) {
                return FormatResult.empty();
            }
            return new FormatResult(sanitized, sanitized);
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat currencyFormat = new DecimalFormat("$#,##0", symbols);
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);

        String formatted = currencyFormat.format(numericValue);
        return new FormatResult(formatted, formatted);
    }

    private Long parseLong(String sanitized) {
        if (sanitized == null) {
            return null;
        }
        String normalized = sanitized.replaceAll("[^0-9-]", "");
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException e) {
            log.debug("Unable to parse [{}] into Long", sanitized);
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object rawValue) {
        if (rawValue instanceof BigDecimal bd) {
            return bd;
        }
        if (rawValue instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (rawValue instanceof CharSequence) {
            String sanitized = sanitizeString(rawValue.toString());
            if (sanitized == null) {
                return null;
            }
            String normalized = sanitized.replaceAll("[,$\\s]", "");
            if (normalized.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(normalized);
            } catch (NumberFormatException e) {
                log.debug("Unable to parse [{}] into BigDecimal", sanitized);
            }
        }
        return null;
    }

    private LocalDate parseToLocalDate(String sanitized) {
        if (sanitized == null) {
            return null;
        }
        try {
            return LocalDate.parse(sanitized);
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(sanitized, DATE_FORMATTER);
            } catch (Exception inner) {
                log.debug("Unable to parse [{}] into LocalDate", sanitized);
                return null;
            }
        }
    }

    private LocalDateTime parseToLocalDateTime(String sanitized) {
        if (sanitized == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(sanitized);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(sanitized, DATE_TIME_FORMATTER);
            } catch (Exception inner) {
                log.debug("Unable to parse [{}] into LocalDateTime", sanitized);
                return null;
            }
        }
    }

    private String sanitizeString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("null")) {
            return null;
        }
        return trimmed;
    }

    public record FormatResult(String csvValue, Object sheetValue) {
        private static final FormatResult EMPTY = new FormatResult("", "");

        public static FormatResult empty() {
            return EMPTY;
        }
    }
}

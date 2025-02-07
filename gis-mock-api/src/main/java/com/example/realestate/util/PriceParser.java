// File: com/example/realestate/util/PriceParser.java

package com.example.realestate.util;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A small helper that knows how to parse a price string like "$398,000" into a double.
 */
public class PriceParser {

    private PriceParser() {
    }

    public static double parsePrice(String priceStr) throws ParseException {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0.0;
        }
        String cleaned = priceStr.replaceAll("[$,]", "");
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(cleaned);
        return number.doubleValue();
    }
}

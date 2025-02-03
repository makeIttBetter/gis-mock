package com.example.realestate.dto;

import lombok.Data;
import java.util.List;

@Data
public class CensusGeocodeResponse {
    private Result result;

    @Data
    public static class Result {
        private List<AddressMatch> addressMatches;
    }

    @Data
    public static class AddressMatch {
        private Coordinates coordinates;
        private String matchedAddress;
        // Additional fields can be added if needed.
    }

    @Data
    public static class Coordinates {
        // Note: The Census response returns "x" (longitude) and "y" (latitude)
        private double x;
        private double y;
    }
}

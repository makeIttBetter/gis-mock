package com.example.realestate.dto;

import lombok.Data;

@Data
public class UtahGeocodeResponse {
    private Result result;
    private int status;

    @Data
    public static class Result {
        private Location location;
        private int score;
        private String locator;
        private String matchAddress;
        private String inputAddress;
        private String standardizedAddress;
        private String addressGrid;
    }

    @Data
    public static class Location {
        private double x;
        private double y;
    }
}

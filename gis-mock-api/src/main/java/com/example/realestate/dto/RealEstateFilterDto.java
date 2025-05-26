// File: src/main/java/com/example/realestate/dto/RealEstateFilterDto.java
package com.example.realestate.dto;

import lombok.Data;

@Data
public class RealEstateFilterDto {
    private String city;
    private String state;
    private String status;
    private Double minPrice;
    private Double maxPrice;
    private String ids;       // Comma-separated list of IDs
    private String address;   // Partial or full address search
    private String zipcode;   // Zipcode filter

    private String propertyTypes; // Comma-separated list of property types
    private String styles;        // Comma-separated list of styles
    private Integer yearBuiltMin;
    private Integer yearBuiltMax;
    private Integer glaMin;
    private Integer glaMax;
    private Integer basementSqFtMin;
    private Integer basementSqFtMax;
    private Boolean basementFinished;
    private Integer daysBackMin;
    private Integer daysBackMax;

    private Double minLat;
    private Double maxLat;
    private Double minLng;
    private Double maxLng;
}

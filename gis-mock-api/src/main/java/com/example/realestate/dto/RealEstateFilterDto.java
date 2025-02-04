package com.example.realestate.dto;

import lombok.Data;

@Data
public class RealEstateFilterDto {
    private String city;
    private String state;
    private String status;
    private Double minPrice;
    private Double maxPrice;
    // New fields:
    private String ids;       // Comma-separated list of ids
    private String address;   // Partial or full address search
    private String zipcode;   // Zipcode filter
}

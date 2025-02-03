// File: src/main/java/com/example/realestate/dto/RealEstateFilterDto.java
package com.example.realestate.dto;

import lombok.Data;

/**
 * This DTO holds optional filters that may come from query parameters.
 */
@Data
public class RealEstateFilterDto {
    private String city;
    private String state;
    private String status;
    private Double minPrice;
    private Double maxPrice;
}

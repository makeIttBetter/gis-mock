package com.example.realestate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A minimal DTO containing only the fields needed by the map,
 * to allow returning many (e.g. 100k) records efficiently.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealEstateMapDto {

    private String id;          // Internal DB ID (still used for linking)
    private String mlsNumber;
    private String taxId;

    private String address;
    private String city;
    private String state;
    private String zip;

    private String soldTerms;
    private String soldPrice;

    private Double latitude;
    private Double longitude;

    private String status;
}

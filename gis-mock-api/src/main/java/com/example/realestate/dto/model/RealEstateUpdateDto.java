package com.example.realestate.dto.model;

import lombok.Data;

/**
 * Fields that can be edited from the map popup.
 * If a field is null, we do not overwrite it in the database.
 */
@Data
public class RealEstateUpdateDto {

    private String soldTerms;
    private String soldPrice;
    private String mlsNumber;
    private String taxId;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String status;

    // We do NOT accept "fullAddress" directly from clients;
    // it is automatically rebuilt on the server whenever address/city/state/zip changes.
}

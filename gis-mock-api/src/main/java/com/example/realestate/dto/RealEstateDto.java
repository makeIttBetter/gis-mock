package com.example.realestate.dto;

import lombok.Data;

@Data
public class RealEstateDto {
    private String id;
    private String mlsNumber;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String status;
    private String listPrice;
    private String latitude;
    private String longitude;
}

package com.example.realestate.dto;

import lombok.Data;

@Data
public class RealEstateCsvErrorDto {
    private int rowNumber;
    private String mlsNumber;
    private String address;
    private String errorMessage;
}

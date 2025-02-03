package com.example.realestate.dto;

import lombok.Data;

import java.util.List;

@Data
public class PolygonCreateDto {
    private String name;
    private List<CoordinateDto> coordinates;
    private List<String> realEstateIds;
}

package com.example.realestate.dto;

import lombok.Data;

import java.util.List;

@Data
public class PolygonDto {
    private String id;
    private String name;
    private List<CoordinateDto> coordinates;
    private List<String> realEstateObjects;
    private String dateCreated;
    private String dateUpdated;
}

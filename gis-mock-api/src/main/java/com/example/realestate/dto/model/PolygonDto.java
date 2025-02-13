package com.example.realestate.dto.model;

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

    private String arcgisLayerId;
    private String arcgisPolygonId;
}

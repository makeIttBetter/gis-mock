package com.example.realestate.dto.google;

import java.util.List;

public record MultiplePolygonsExportRequest(List<String> polygonIds, String sheetName) {
}
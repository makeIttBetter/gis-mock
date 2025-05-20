package com.example.realestate.controller.google;

import com.example.realestate.dto.google.MultiplePolygonsExportRequest;
import com.example.realestate.dto.google.PolygonExportRequest;
import com.example.realestate.service.google.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/google/sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

    private final GoogleSheetsService googleSheetsService;

    @PostMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> exportPolygonToSheets(@RequestBody PolygonExportRequest req) {
        String polygonId = req.polygonId();
        log.info("REST: /api/google/sheets/export; polygonId={}", polygonId);
        try {
            String sheetsUrl = googleSheetsService.exportPolygonDataToSheets(polygonId);
            return ResponseEntity.ok("Polygon exported to Google Sheets successfully. URL: " + sheetsUrl);
        } catch (Exception e) {
            log.error("Failed to export polygon to Google Sheets", e);
            return ResponseEntity.status(500).body("Failed to export Polygon to Google Sheets: " + e.getMessage());
        }
    }

    @PostMapping("/export-multiple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> exportMultiplePolygonsToSheets(@RequestBody MultiplePolygonsExportRequest req) {
        List<String> polygonIds = req.polygonIds();
        String sheetName = req.sheetName();
        log.info("REST: /api/google/sheets/export-multiple; polygonIds={}, sheetName={}", polygonIds, sheetName);
        try {
            String sheetsUrl = googleSheetsService.exportMultiplePolygonsToSheets(polygonIds, sheetName);
            return ResponseEntity.ok("Polygons exported to Google Sheets successfully. URL: " + sheetsUrl);
        } catch (Exception e) {
            log.error("Failed to export multiple polygons to Google Sheets", e);
            return ResponseEntity.status(500).body("Failed to export Polygons to Google Sheets: " + e.getMessage());
        }
    }
}
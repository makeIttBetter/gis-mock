package com.example.realestate.controller.google;

import com.example.realestate.dto.google.PolygonExportRequest;
import com.example.realestate.service.google.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/google/oauth2")
@RequiredArgsConstructor
public class GoogleSheetsController {

    private final GoogleSheetsService googleSheetsService;

    /**
     * Step 3: Export polygon data to Google Sheets (stub).
     */
    @PostMapping("/sheets/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> exportPolygonToSheets(@RequestBody PolygonExportRequest req) {
        String polygonId = req.polygonId();
        log.info("REST: /api/google/oauth2/sheets/export; polygonId={}", polygonId);
        boolean success = googleSheetsService.exportPolygonDataToSheets(polygonId);
        if (success) {
            return ResponseEntity.ok("Polygon exported to Google Sheets successfully.");
        } else {
            return ResponseEntity.status(500).body("Failed to export Polygon to Google Sheets.");
        }
    }
}

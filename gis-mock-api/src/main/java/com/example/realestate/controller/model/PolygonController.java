package com.example.realestate.controller.model;

import com.example.realestate.dto.model.PolygonCreateDto;
import com.example.realestate.dto.model.PolygonDto;
import com.example.realestate.export.service.RealEstateCsvExportService;
import com.example.realestate.model.RealEstate;
import com.example.realestate.service.PolygonService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/polygons")
public class PolygonController {

    private final PolygonService polygonService;
    private final RealEstateCsvExportService realEstateCsvExportService;

    public PolygonController(
            PolygonService polygonService,
            RealEstateCsvExportService realEstateCsvExportService
    ) {
        this.polygonService = polygonService;
        this.realEstateCsvExportService = realEstateCsvExportService;
    }

    @PostMapping
    public ResponseEntity<PolygonDto> createPolygon(@RequestBody PolygonCreateDto payload) {
        log.info("POST /api/polygons {}", payload);
        PolygonDto created = polygonService.create(payload);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolygonDto> getPolygon(@PathVariable(name = "id") String id) {
        log.info("GET /api/polygons/{}", id);
        PolygonDto polygon = polygonService.getByIdForCurrentUser(id);
        if (polygon == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(polygon);
    }

    @GetMapping
    public ResponseEntity<List<PolygonDto>> getPolygons() {
        log.info("GET /api/polygons");
        List<PolygonDto> polygons = polygonService.getAll();
        return ResponseEntity.ok(polygons);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePolygon(@PathVariable(name = "id") String id) {
        log.info("DELETE /api/polygons/{}", id);
        polygonService.delete(id);
        return ResponseEntity.ok().build();
    }

    // NEW: Update endpoint for editing an existing polygon
    @PutMapping("/{id}")
    public ResponseEntity<PolygonDto> updatePolygon(@PathVariable("id") String id,
                                                    @RequestBody PolygonCreateDto payload) {
        log.info("PUT /api/polygons/{} with payload {}", id, payload);
        // Construct a PolygonDto from the payload
        PolygonDto updateDto = new PolygonDto();
        updateDto.setId(id);
        updateDto.setName(payload.getName());
        updateDto.setCoordinates(payload.getCoordinates());
        updateDto.setRealEstateObjects(payload.getRealEstateIds());

        PolygonDto updated = polygonService.update(id, updateDto);
        if (updated == null) {
            // e.g. polygon not found or not owned by current user
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * NEW ENDPOINT: Exports all RealEstate attached to this polygon as CSV.
     * The frontend calls GET /api/polygons/{id}/export/csv
     */
    @GetMapping("/{id}/export/csv")
    public void exportPolygonCsv(@PathVariable("id") String polygonId,
                                 HttpServletResponse response) throws IOException {
        log.info("GET /api/polygons/{}/export/csv", polygonId);

        // 1) Get the polygon. If not found, return 404
        PolygonDto polygonDto = polygonService.getByIdForCurrentUser(polygonId);
        if (polygonDto == null) {
            log.warn("Polygon not found or not authorized for polygonId={}", polygonId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Polygon not found");
            return;
        }

        // 2) Look up all RealEstate attached to this polygon
        List<RealEstate> realEstates = polygonService.findAttachedRealEstates(polygonId);

        // 3) Use the RealEstateCsvExportService to write CSV directly to response
        realEstateCsvExportService.exportToCsv(realEstates, polygonDto, response);
    }
}

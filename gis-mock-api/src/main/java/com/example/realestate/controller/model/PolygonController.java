package com.example.realestate.controller.model;

import com.example.realestate.dto.PolygonCreateDto;
import com.example.realestate.dto.PolygonDto;
import com.example.realestate.service.PolygonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/polygons")
public class PolygonController {

    private final PolygonService polygonService;

    public PolygonController(PolygonService polygonService) {
        this.polygonService = polygonService;
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
        PolygonDto polygon = polygonService.getById(id);
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
    public ResponseEntity<PolygonDto> updatePolygon(@PathVariable("id") String id, @RequestBody PolygonCreateDto payload) {
        log.info("PUT /api/polygons/{} with payload {}", id, payload);
        // Construct a PolygonDto from the payload (only updating name, coordinates, and real estate IDs)
        PolygonDto updateDto = new PolygonDto();
        updateDto.setId(id);
        updateDto.setName(payload.getName());
        updateDto.setCoordinates(payload.getCoordinates());
        updateDto.setRealEstateObjects(payload.getRealEstateIds());
        PolygonDto updated = polygonService.update(id, updateDto);
        return ResponseEntity.ok(updated);
    }
}

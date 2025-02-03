package com.example.realestate.controller;

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
}

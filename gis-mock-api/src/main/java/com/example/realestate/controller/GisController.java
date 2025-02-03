package com.example.realestate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/gis")
public class GisController {

    @GetMapping("/mock-dots")
    public Map<String, Object> getMockDots() {
        log.info("GET /api/gis/mock-dots");
        // Mock GeoJSON data
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");

        List<Map<String, Object>> features = List.of(
                createFeature(-111.6706, 40.2969), // Example point in Orem, Utah
                createFeature(-111.685, 40.3047),
                createFeature(-111.66, 40.3124)
        );

        geoJson.put("features", features);
        return geoJson;
    }

    private Map<String, Object> createFeature(double longitude, double latitude) {
        log.info("Creating feature at {}, {}", longitude, latitude);
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Point");
        geometry.put("coordinates", List.of(longitude, latitude));

        feature.put("geometry", geometry);

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Mock Point");

        feature.put("properties", properties);
        return feature;
    }
}

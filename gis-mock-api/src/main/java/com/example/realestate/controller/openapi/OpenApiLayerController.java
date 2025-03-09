package com.example.realestate.controller.openapi;

import com.example.realestate.dto.model.CoordinateDto;
import com.example.realestate.dto.model.PolygonDto;
import com.example.realestate.service.PolygonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * This controller serves GeoJSON layer data for a given polygon.
 * Base path: /openApi/layers
 */
@Slf4j
@RestController
@RequestMapping("/api/openApi/layers")
public class OpenApiLayerController {

    private final PolygonService polygonService;

    public OpenApiLayerController(PolygonService polygonService) {
        this.polygonService = polygonService;
    }

    /**
     * Get GeoJSON data for a single polygon.
     * @param polygonId The ID of the polygon to fetch.
     * @return GeoJSON data for the polygon.
     */
    @GetMapping(
            value = "/{polygonId}/polygon-coordinates",
            produces = "application/geo+json"
    )
    public ResponseEntity<Map<String, Object>> getPolygonCoordinates(
            @PathVariable("polygonId") String polygonId
    ) {
        // Fetch the polygon data
        PolygonDto polygonDto = polygonService.getById(polygonId);
        if (polygonDto == null) {
            return ResponseEntity.notFound().build();
        }

        // Convert coordinates to GeoJSON Polygon format
        List<List<Double>> linearRing = new ArrayList<>();
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            linearRing.add(List.of(coord.getLng(), coord.getLat()));
        }
        // Ensure the polygon is closed (first and last points are the same)
        if (!linearRing.isEmpty() && !linearRing.get(0).equals(linearRing.get(linearRing.size() - 1))) {
            linearRing.add(new ArrayList<>(linearRing.get(0)));
        }
        List<List<List<Double>>> coordinates = List.of(linearRing);

        // Create a single Polygon Feature
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        // Define the Polygon geometry
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Polygon");
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);

        // Add properties (customize as needed)
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", polygonDto.getId());
        properties.put("name", polygonDto.getName() != null ? polygonDto.getName() : "Polygon " + polygonId);
        feature.put("properties", properties);

        // Wrap in a FeatureCollection
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", List.of(feature));

        return ResponseEntity.ok(featureCollection);
    }

    /**
     * Get GeoJSON data for a single polygon's object locations.
     * @param polygonId The ID of the polygon to fetch.
     * @return GeoJSON data for the polygon's vertices.
     */
    @GetMapping(
            value = "/{polygonId}/data-set",
            produces = "application/geo+json"
    )
    public ResponseEntity<Map<String, Object>> getPolygonDataSet(
            @PathVariable("polygonId") String polygonId
    ) {
        // Fetch the polygon data
        PolygonDto polygonDto = polygonService.getById(polygonId);
        if (polygonDto == null) {
            return ResponseEntity.notFound().build();
        }

        // Create list of point features
        List<Map<String, Object>> features = new ArrayList<>();
        int index = 1;
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            // Create a single Point Feature
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");

            // Define the Point geometry
            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "Point");
            geometry.put("coordinates", List.of(coord.getLng(), coord.getLat()));
            feature.put("geometry", geometry);

            // Add properties (customize as needed)
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "Vertex " + index++);
            properties.put("polygonId", polygonDto.getId());
            feature.put("properties", properties);

            features.add(feature);
        }

        // Wrap in a FeatureCollection
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", features);

        return ResponseEntity.ok(featureCollection);
    }
}

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
     * GET /openApi/layers/{polygonId}
     * Returns the polygon data in single "Feature" GeoJSON format.
     *
     * @param polygonId the ID of the polygon
     * @return GeoJSON Feature representing the polygon
     */
    @GetMapping(value = "/{polygonId}", produces = "application/geo+json")
    public ResponseEntity<Map<String, Object>> getLayerGeoJson(@PathVariable("polygonId") String polygonId) {
        log.info("Fetching GeoJSON (single Feature) for polygonId: {}", polygonId);
        PolygonDto polygonDto = polygonService.getById(polygonId);
        if (polygonDto == null) {
            log.warn("Polygon with ID {} not found", polygonId);
            return ResponseEntity.notFound().build();
        }

        // Convert the polygon's coordinates into a GeoJSON ring (array of [lng, lat])
        List<List<Double>> linearRing = new ArrayList<>();
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            // In GeoJSON: [longitude, latitude]
            linearRing.add(List.of(coord.getLng(), coord.getLat()));
        }
        // Ensure ring is closed
        if (!linearRing.isEmpty() && !linearRing.get(0).equals(linearRing.get(linearRing.size() - 1))) {
            linearRing.add(new ArrayList<>(linearRing.get(0)));
        }
        List<List<List<Double>>> coordinates = List.of(linearRing);

        // Build the single Feature
        Map<String, Object> geoJson = new LinkedHashMap<>();
        geoJson.put("type", "Feature");

        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "Polygon");
        geometry.put("coordinates", coordinates);
        geoJson.put("geometry", geometry);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", polygonDto.getId());
        properties.put("name", polygonDto.getName());
        // add more properties if needed
        geoJson.put("properties", properties);

        return ResponseEntity.ok(geoJson);
    }

    /**
     * NEW ENDPOINT: Returns the polygon data wrapped in a FeatureCollection,
     * with exactly one Feature inside "features".
     * Example path: GET /openApi/layers/{polygonId}/feature-collection
     * Produces: application/geo+json
     */
    @GetMapping(
            value = "/{polygonId}/feature-collection",
            produces = "application/geo+json"
    )
    public ResponseEntity<Map<String, Object>> getPolygonAsFeatureCollection(
            @PathVariable("polygonId") String polygonId
    ) {
        log.info("Fetching GeoJSON as FeatureCollection for polygonId={}", polygonId);
        PolygonDto polygonDto = polygonService.getById(polygonId);
        if (polygonDto == null) {
            log.warn("Polygon with ID {} not found", polygonId);
            return ResponseEntity.notFound().build();
        }

        // 1) Convert polygon coords => ring
        List<List<Double>> linearRing = new ArrayList<>();
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            linearRing.add(List.of(coord.getLng(), coord.getLat()));
        }
        if (!linearRing.isEmpty() && !linearRing.get(0).equals(linearRing.get(linearRing.size() - 1))) {
            linearRing.add(new ArrayList<>(linearRing.get(0)));
        }
        List<List<List<Double>>> coordinates = List.of(linearRing);

        // 2) Create single Feature object
        Map<String, Object> singleFeature = new HashMap<>();
        singleFeature.put("type", "Feature");

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Polygon");
        geometry.put("coordinates", coordinates);
        singleFeature.put("geometry", geometry);

        Map<String, Object> properties = new HashMap<>();
        properties.put("id", polygonDto.getId());
        properties.put("name", polygonDto.getName());
        singleFeature.put("properties", properties);

        // 3) Wrap into a FeatureCollection
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", List.of(singleFeature));

        return ResponseEntity.ok(featureCollection);
    }
}

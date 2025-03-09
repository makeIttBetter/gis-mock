package com.example.realestate.controller;

import com.example.realestate.dto.model.CoordinateDto;
import com.example.realestate.dto.model.PolygonDto;
import com.example.realestate.service.PolygonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * This controller serves GeoJSON layer data for a given polygon, matching the same JSON structure
 * that the GisController produces, so ArcGIS can read it as a feature layer.
 * Base path: /api/openApi/layers
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
     * Rounds a single double value to 6 decimal places.
     *
     * @param value The original coordinate value.
     * @return The rounded value.
     */
    private double roundCoordinate(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Rounds the longitude and latitude of the given CoordinateDto and returns them as a list.
     *
     * @param coord The coordinate data.
     * @return A list containing the rounded longitude and latitude.
     */
    private List<Double> roundCoordinates(CoordinateDto coord) {
        double roundedLng = roundCoordinate(coord.getLng());
        double roundedLat = roundCoordinate(coord.getLat());
        return List.of(roundedLng, roundedLat);
    }

    /**
     * Get GeoJSON data for a single polygon in the same structure as /api/gis/mock-polygon.
     * @param polygonId The ID of the polygon to fetch.
     * @return A FeatureCollection with a single Polygon Feature.
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

        // Build the outer ring for the polygon with rounded coordinates
        List<List<Double>> ring = new ArrayList<>();
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            ring.add(roundCoordinates(coord));
        }
        // Ensure the ring is closed by repeating the first coordinate at the end
        if (!ring.isEmpty() && !ring.get(0).equals(ring.get(ring.size() - 1))) {
            ring.add(new ArrayList<>(ring.get(0)));
        }

        // Build the GeoJSON Feature for the polygon
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        // Polygon geometry
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Polygon");
        // coordinates -> [ [ ring ] ]
        geometry.put("coordinates", List.of(ring));
        feature.put("geometry", geometry);

        // properties (similar to GisController)
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", polygonDto.getName() != null ? polygonDto.getName() : "Polygon " + polygonId);
        feature.put("properties", properties);

        // Wrap in a FeatureCollection
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", List.of(feature));

        return ResponseEntity.ok(featureCollection);
    }

    /**
     * Get GeoJSON data for a polygon's vertices (points) in the same structure as /api/gis/mock-dots.
     * @param polygonId The ID of the polygon to fetch.
     * @return A FeatureCollection of point Features (one for each vertex).
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

        // Build a list of Features, one Point per vertex with rounded coordinates
        List<Map<String, Object>> features = new ArrayList<>();
        int index = 1;
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");

            // Geometry for a Point
            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "Point");
            // coordinates -> [longitude, latitude] with rounded values
            geometry.put("coordinates", roundCoordinates(coord));
            feature.put("geometry", geometry);

            // properties
            Map<String, Object> properties = new HashMap<>();
            properties.put("OBJECTID", index);
            properties.put("name", "Vertex " + index);
            feature.put("properties", properties);

            features.add(feature);
            index++;
        }

        // Wrap in a FeatureCollection
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", features);

        return ResponseEntity.ok(featureCollection);
    }
}

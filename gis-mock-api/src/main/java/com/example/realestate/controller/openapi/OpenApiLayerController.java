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
 * This controller serves ArcGIS JSON layer data for a given polygon.
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
     * Get ArcGIS JSON data for a single polygon.
     * @param polygonId The ID of the polygon to fetch.
     * @return ArcGIS JSON data for the polygon.
     */
    @GetMapping(
            value = "/{polygonId}/polygon-coordinates",
            produces = "application/json"
    )
    public ResponseEntity<Map<String, Object>> getPolygonCoordinates(
            @PathVariable("polygonId") String polygonId
    ) {
        // Fetch the polygon data
        PolygonDto polygonDto = polygonService.getById(polygonId);
        if (polygonDto == null) {
            return ResponseEntity.notFound().build();
        }

        // Convert coordinates to ArcGIS Polygon rings format
        List<List<Double>> ring = new ArrayList<>();
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            ring.add(List.of(coord.getLng(), coord.getLat()));
        }
        // Ensure the polygon is closed (first and last points are the same)
        if (!ring.isEmpty() && !ring.get(0).equals(ring.get(ring.size() - 1))) {
            ring.add(new ArrayList<>(ring.get(0)));
        }

        // Create a single Feature for the polygon in ArcGIS JSON format
        Map<String, Object> feature = new HashMap<>();

        // Build attributes (using OBJECTID as 1 for a single polygon)
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("OBJECTID", 1);
        attributes.put("name", polygonDto.getName() != null ? polygonDto.getName() : "Polygon " + polygonId);
        feature.put("attributes", attributes);

        // Build geometry with rings (ArcGIS requires "rings" for polygons)
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("rings", List.of(ring));
        feature.put("geometry", geometry);

        // Build the Feature Collection with extra ArcGIS metadata
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("objectIdField", "OBJECTID");
        featureCollection.put("geometryType", "esriGeometryPolygon");
        featureCollection.put("spatialReference", Map.of("wkid", 4326));
        featureCollection.put("fields", List.of(
                Map.of("name", "OBJECTID", "type", "esriFieldTypeOID"),
                Map.of("name", "name", "type", "esriFieldTypeString")
        ));
        featureCollection.put("features", List.of(feature));

        return ResponseEntity.ok(featureCollection);
    }

    /**
     * Get ArcGIS JSON data for a single polygon's vertices (point features).
     * @param polygonId The ID of the polygon to fetch.
     * @return ArcGIS JSON data for the polygon's vertices.
     */
    @GetMapping(
            value = "/{polygonId}/data-set",
            produces = "application/json"
    )
    public ResponseEntity<Map<String, Object>> getPolygonDataSet(
            @PathVariable("polygonId") String polygonId
    ) {
        // Fetch the polygon data
        PolygonDto polygonDto = polygonService.getById(polygonId);
        if (polygonDto == null) {
            return ResponseEntity.notFound().build();
        }

        // Create list of point features using ArcGIS JSON format
        List<Map<String, Object>> features = new ArrayList<>();
        int index = 1;
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            Map<String, Object> feature = new HashMap<>();

            // Build attributes for the point feature
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("OBJECTID", index); // Use index as OBJECTID
            attributes.put("name", "Vertex " + index);
            attributes.put("polygonId", polygonDto.getId());
            feature.put("attributes", attributes);

            // Build geometry for a point (x: longitude, y: latitude)
            Map<String, Object> geometry = new HashMap<>();
            geometry.put("x", coord.getLng());
            geometry.put("y", coord.getLat());
            feature.put("geometry", geometry);

            features.add(feature);
            index++;
        }

        // Build the Feature Collection for point features with extra ArcGIS metadata
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("objectIdField", "OBJECTID");
        featureCollection.put("geometryType", "esriGeometryPoint");
        featureCollection.put("spatialReference", Map.of("wkid", 4326));
        featureCollection.put("fields", List.of(
                Map.of("name", "OBJECTID", "type", "esriFieldTypeOID"),
                Map.of("name", "name", "type", "esriFieldTypeString"),
                Map.of("name", "polygonId", "type", "esriFieldTypeString")
        ));
        featureCollection.put("features", features);

        return ResponseEntity.ok(featureCollection);
    }
}

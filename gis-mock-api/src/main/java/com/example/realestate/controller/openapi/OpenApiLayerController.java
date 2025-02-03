package com.example.realestate.controller.openapi;

import com.example.realestate.dto.CoordinateDto;
import com.example.realestate.dto.PolygonDto;
import com.example.realestate.service.PolygonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * This controller serves GeoJSON layer data for a given polygon.
 * It listens on the /openApi/layers/{polygonId} endpoint.
 */
@Slf4j
@RestController
@RequestMapping("/openApi/layers")
public class OpenApiLayerController {

    private final PolygonService polygonService;

    public OpenApiLayerController(PolygonService polygonService) {
        this.polygonService = polygonService;
    }

    /**
     * GET /openApi/layers/{polygonId}
     * Returns the polygon data in GeoJSON format.
     *
     * @param polygonId the ID of the polygon
     * @return GeoJSON Feature representing the polygon
     */
    @GetMapping("/{polygonId}")
    public ResponseEntity<Map<String, Object>> getLayerGeoJson(@PathVariable("polygonId") String polygonId) {
        log.info("Fetching GeoJSON for polygonId: {}", polygonId);
        PolygonDto polygonDto = polygonService.getById(polygonId);
        if (polygonDto == null) {
            log.warn("Polygon with ID {} not found", polygonId);
            return ResponseEntity.notFound().build();
        }

        // Convert the polygon's coordinates into a GeoJSON linear ring (an array of [lng, lat] pairs)
        List<List<Double>> linearRing = new ArrayList<>();
        for (CoordinateDto coord : polygonDto.getCoordinates()) {
            // In GeoJSON, coordinates are specified as [longitude, latitude]
            linearRing.add(List.of(coord.getLng(), coord.getLat()));
        }
        // Ensure the polygon ring is closed (first coordinate equals last coordinate)
        if (!linearRing.isEmpty() && !linearRing.get(0).equals(linearRing.get(linearRing.size() - 1))) {
            linearRing.add(new ArrayList<>(linearRing.get(0)));
        }
        // GeoJSON polygons expect an array of rings, so we wrap our ring in another list.
        List<List<List<Double>>> coordinates = List.of(linearRing);

        // Build the GeoJSON Feature
        Map<String, Object> geoJson = new LinkedHashMap<>();
        geoJson.put("type", "Feature");

        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "Polygon");
        geometry.put("coordinates", coordinates);
        geoJson.put("geometry", geometry);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", polygonDto.getId());
        properties.put("name", polygonDto.getName());
        // You may add additional properties if needed.
        geoJson.put("properties", properties);

        return ResponseEntity.ok(geoJson);
    }
}

package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gis")
public class GisController {

    private static int idCounter = 1;

    @GetMapping(value = "/mock-dots", produces = "application/geo+json")
    public Map<String, Object> getMockDots() {
        // (Existing endpoint that returns point features)
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
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Point");
        geometry.put("coordinates", List.of(longitude, latitude));
        feature.put("geometry", geometry);

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Mock Point");
        // Add a unique identifier
        properties.put("OBJECTID", idCounter++);
        feature.put("properties", properties);
        return feature;
    }


    // New endpoint that returns a polygon as a FeatureCollection
    @GetMapping(value = "/mock-polygon", produces = "application/geo+json")
    public Map<String, Object> getMockPolygon() {
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        // Create a list with a single polygon feature
        List<Map<String, Object>> features = List.of(createPolygonFeature());
        featureCollection.put("features", features);
        return featureCollection;
    }

    // Helper method to create a polygon feature
    private Map<String, Object> createPolygonFeature() {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        // Define the polygon geometry as a rectangle (with first and last coordinate the same)
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Polygon");
        List<List<List<Double>>> coordinates = List.of(
                List.of(
                        List.of(-111.70, 40.30),
                        List.of(-111.68, 40.30),
                        List.of(-111.68, 40.32),
                        List.of(-111.70, 40.32),
                        List.of(-111.70, 40.30)  // Closes the polygon
                )
        );
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);

        // Add any properties you need
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Mock Polygon");
        feature.put("properties", properties);

        return feature;
    }
}

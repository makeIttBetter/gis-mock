package com.example.realestate.service;

import com.example.realestate.client.arcgis.ArcgisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Service for managing ArcGIS layers.
 * Updated to create a GeoJSON layer referencing an external data URL.
 */
@Slf4j
@Service
public class ArcgisLayerService {

    private final ArcgisClient arcgisClient;
    @Value("${arcgis.username}")
    private String arcgisUsername;
    @Value("${arcgis.api.key}")
    private String arcgisApiKey;

    public ArcgisLayerService(ArcgisClient arcgisClient) {
        this.arcgisClient = arcgisClient;
    }

    /**
     * Creates a new GeoJSON layer in ArcGIS Online that references an external data URL.
     *
     * @param layerName The name to display for the layer.
     * @param dataUrl   The external URL containing the GeoJSON data.
     * @return the ArcGIS item ID returned by ArcGIS.
     */
    public String createLayer(String layerName, String dataUrl) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("title", layerName);
        // For polygons, use "GeoJson"; we remain consistent with the type used previously.
        formData.add("type", "GeoJson");
        formData.add("url", dataUrl);
        formData.add("description", "GeoJSON layer created by RealEstate App: " + layerName);

        log.info("Creating ArcGIS layer (GeoJSON) with name: {} and URL: {}", layerName, dataUrl);
        try {
            Map<String, Object> response =
                    arcgisClient.addItem(arcgisUsername, "json", arcgisApiKey, formData);

            Object successObj = response.get("success");
            boolean success = false;
            if (successObj instanceof Boolean) {
                success = (Boolean) successObj;
            } else if (successObj != null) {
                success = Boolean.parseBoolean(successObj.toString());
            }
            if (success) {
                String itemId = (String) response.get("id");
                log.info("ArcGIS layer created with itemId: {}", itemId);
                return itemId;
            } else {
                log.error("Failed to create ArcGIS layer. Response: {}", response);
                throw new RuntimeException("Failed to create ArcGIS layer");
            }
        } catch (Exception e) {
            log.error("Exception while creating ArcGIS layer", e);
            throw new RuntimeException("Exception while creating ArcGIS layer", e);
        }
    }

    /**
     * Deletes an ArcGIS layer/item by ID.
     */
    public void deleteLayer(String itemId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("items", itemId);

        log.info("Deleting ArcGIS layer with itemId: {}", itemId);
        try {
            Map<String, Object> response =
                    arcgisClient.deleteItems(arcgisUsername, "json", arcgisApiKey, formData);

            Object resultsObj = response.get("results");
            boolean success = false;
            if (resultsObj instanceof java.util.List) {
                java.util.List<?> resultsList = (java.util.List<?>) resultsObj;
                for (Object result : resultsList) {
                    if (result instanceof Map) {
                        Map<?, ?> resultMap = (Map<?, ?>) result;
                        if (itemId.equals(resultMap.get("itemId"))) {
                            Object successObj = resultMap.get("success");
                            if (successObj instanceof Boolean) {
                                success = (Boolean) successObj;
                            } else if (successObj != null) {
                                success = Boolean.parseBoolean(successObj.toString());
                            }
                            break;
                        }
                    }
                }
            }
            if (success) {
                log.info("ArcGIS layer with itemId {} deleted successfully", itemId);
            } else {
                log.error("Failed to delete ArcGIS layer. Response: {}", response);
                throw new RuntimeException("Failed to delete ArcGIS layer");
            }
        } catch (Exception e) {
            log.error("Exception while deleting ArcGIS layer", e);
            throw new RuntimeException("Exception while deleting ArcGIS layer", e);
        }
    }

    /**
     * NEW: Updates an ArcGIS layer's title (i.e., renames the ArcGIS item).
     *
     * @param itemId  ArcGIS item ID to rename.
     * @param newName The new title for the item.
     */
    public void updateLayerName(String itemId, String newName) {
        log.info("Renaming ArcGIS layer with itemId='{}' to '{}'", itemId, newName);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        // ArcGIS typically uses "title" as the field for item name:
        formData.add("title", newName);

        try {
            Map<String, Object> response =
                    arcgisClient.updateItem(
                            arcgisUsername,
                            itemId,
                            "json",
                            arcgisApiKey,
                            formData
                    );

            boolean success = parseSuccess(response.get("success"));
            if (success) {
                log.info("ArcGIS layer itemId={} updated (renamed) successfully", itemId);
            } else {
                log.error("Failed to rename ArcGIS layer. Response: {}", response);
                throw new RuntimeException("Failed to rename ArcGIS layer itemId=" + itemId);
            }
        } catch (Exception e) {
            log.error("Exception while renaming ArcGIS layer", e);
            throw new RuntimeException("Exception while renaming ArcGIS layer", e);
        }
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------
    private boolean parseSuccess(Object successObj) {
        if (successObj instanceof Boolean) {
            return (Boolean) successObj;
        } else if (successObj != null) {
            return Boolean.parseBoolean(successObj.toString());
        }
        return false;
    }
}

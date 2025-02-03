package com.example.realestate.service;

import com.example.realestate.client.ArcgisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Service for managing ArcGIS layers.
 * Updated to create a GeoJSON layer instead of a CSV layer.
 */
@Slf4j
@Service
public class ArcgisLayerService {

    @Value("${arcgis.username}")
    private String arcgisUsername;

    @Value("${arcgis.api.key}")
    private String arcgisApiKey;

    private final ArcgisClient arcgisClient;

    public ArcgisLayerService(ArcgisClient arcgisClient) {
        this.arcgisClient = arcgisClient;
    }

    /**
     * Creates a new GeoJSON layer in ArcGIS Online that references an external data URL.
     *
     * @param polygonName The name of the new layer.
     * @param dataUrl     The external URL containing the GeoJSON data (e.g. /openApi/layers/{polygonId}).
     * @return the ArcGIS item ID (layer ID) returned by ArcGIS.
     */
    public String createLayer(String polygonName, String dataUrl) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("title", polygonName);
        // Change type from "CSV" to "GeoJson" for creating a GeoJSON layer.
        formData.add("type", "GeoJson");
        // TODO: Update the URL to point to your application's endpoint that serves GeoJSON data.
        formData.add("url", "https://mytestapp.online/api/gis/mock-dots");
        formData.add("description", "GeoJSON layer created by RealEstate App for polygon: " + polygonName);

        log.info("Creating ArcGIS layer (GeoJSON) with name: {} in default folder", polygonName);
        try {
            Map<String, Object> response = arcgisClient.addItem(arcgisUsername, "json", arcgisApiKey, formData);
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

    public void deleteLayer(String itemId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("items", itemId);

        log.info("Deleting ArcGIS layer with itemId: {}", itemId);
        try {
            Map<String, Object> response = arcgisClient.deleteItems(arcgisUsername, "json", arcgisApiKey, formData);
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
}

// File: src/main/java/com/example/realestate/client/ArcgisClient.java
package com.example.realestate.client.arcgis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "arcgisClient", url = "${arcgis.base-url}", configuration = ArcgisClientConfig.class)
public interface ArcgisClient {

    /**
     * Adds items to the user's ArcGIS Online account.
     * Typically: POST /content/users/{username}/addItem
     */
    @PostMapping(value = "/content/users/{username}/addItem", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> addItem(@PathVariable("username") String username,
                                @RequestParam("f") String f,
                                @RequestParam("token") String token,
                                @RequestBody MultiValueMap<String, String> formData);

    /**
     * Deletes items from the user's ArcGIS Online account.
     * Typically: POST /content/users/{username}/deleteItems
     */
    @PostMapping(value = "/content/users/{username}/deleteItems", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> deleteItems(@PathVariable("username") String username,
                                    @RequestParam("f") String f,
                                    @RequestParam("token") String token,
                                    @RequestBody MultiValueMap<String, String> formData);

    /**
     * Updates an existing ArcGIS item by ID.
     * Typically: POST /content/users/{username}/items/{itemId}/update
     */
    @PostMapping(
            value = "/content/users/{username}/items/{itemId}/update",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Map<String, Object> updateItem(
            @PathVariable("username") String username,
            @PathVariable("itemId") String itemId,
            @RequestParam("f") String f,
            @RequestParam("token") String token,
            @RequestBody MultiValueMap<String, String> formData
    );
}

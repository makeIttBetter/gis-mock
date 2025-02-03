package com.example.realestate.client;

import com.example.realestate.dto.UtahGeocodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "utahMapServiceClient", url = "${mapserv.utah.base-url}")
public interface UtahMapServiceClient {

    // Example URL: /api/v1/geocode/{address}/{zip}?apikey={apikey}
    @GetMapping("/api/v1/geocode/{address}/{zip}")
    UtahGeocodeResponse geocode(@PathVariable("address") String address,
                                @PathVariable("zip") String zip,
                                @RequestParam("apikey") String apikey);
}

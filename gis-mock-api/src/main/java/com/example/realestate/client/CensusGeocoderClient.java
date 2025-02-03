package com.example.realestate.client;

import com.example.realestate.dto.CensusGeocodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "censusGeocoderClient", url = "https://geocoding.geo.census.gov")
public interface CensusGeocoderClient {

    // Calls the one‑line address endpoint.
    @GetMapping("/geocoder/locations/onelineaddress")
    CensusGeocodeResponse geocode(@RequestParam("address") String address,
                                  @RequestParam("benchmark") String benchmark,
                                  @RequestParam("format") String format);
}

// File: src/main/java/com/example/realestate/controller/RealEstateController.java
package com.example.realestate.controller;

import com.example.realestate.dto.RealEstateDto;
import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.service.RealEstateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * REST Controller that exposes endpoints to retrieve real estate data.
 */
@Slf4j
@RestController
@RequestMapping("/api/real-estate")
public class RealEstateController {

    private final RealEstateService realEstateService;

    public RealEstateController(RealEstateService realEstateService) {
        this.realEstateService = realEstateService;
    }

    @GetMapping
    public List<RealEstateDto> getRealEstate(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice
    ) {
        log.info("GET /api/real-estate?city={}&state={}&status={}&minPrice={}&maxPrice={}",
                city, state, status, minPrice, maxPrice);
        RealEstateFilterDto filterDto = new RealEstateFilterDto();
        filterDto.setCity(city);
        filterDto.setState(state);
        filterDto.setStatus(status);
        filterDto.setMinPrice(minPrice);
        filterDto.setMaxPrice(maxPrice);
        return realEstateService.getFilteredRealEstate(filterDto);
    }

    // NEW: Endpoint to get detailed information for real estate records by a list of IDs.
    @GetMapping("/attached")
    public ResponseEntity<List<RealEstateDto>> getAttachedRealEstate(@RequestParam("ids") String ids) {
        log.info("GET /api/real-estate/attached with ids: {}", ids);
        List<String> idList = Arrays.asList(ids.split(","));
        List<RealEstateDto> attachedRecords = realEstateService.getRealEstateByIds(idList);
        return ResponseEntity.ok(attachedRecords);
    }
}

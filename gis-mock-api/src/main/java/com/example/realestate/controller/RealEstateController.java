// File: src/main/java/com/example/realestate/controller/RealEstateController.java
package com.example.realestate.controller;

import com.example.realestate.dto.RealEstateDto;
import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.service.RealEstateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller that exposes an endpoint to retrieve real estate data,
 * optionally filtered by query params.
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
        // Build filter DTO from query parameters
        RealEstateFilterDto filterDto = new RealEstateFilterDto();
        filterDto.setCity(city);
        filterDto.setState(state);
        filterDto.setStatus(status);
        filterDto.setMinPrice(minPrice);
        filterDto.setMaxPrice(maxPrice);
        return realEstateService.getFilteredRealEstate(filterDto);
    }
}

// File: src/main/java/com/example/realestate/controller/RealEstateFilterOptionsController.java
package com.example.realestate.controller;

import com.example.realestate.service.RealEstateFilterOptionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/real-estate/filters")
public class RealEstateFilterOptionsController {

    private final RealEstateFilterOptionsService filterOptionsService;

    public RealEstateFilterOptionsController(RealEstateFilterOptionsService filterOptionsService) {
        this.filterOptionsService = filterOptionsService;
    }

    @GetMapping("/{field}")
    public ResponseEntity<List<String>> getFilterOptions(@PathVariable("field") String field) {
        log.info("Fetching distinct filter options for field: {}", field);
        try {
            List<String> options = filterOptionsService.getDistinctValues(field);
            return ResponseEntity.ok(options);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}

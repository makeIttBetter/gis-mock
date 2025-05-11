package com.example.realestate.controller.model;

import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.dto.RealEstateMapDto;
import com.example.realestate.dto.model.PaginatedResponseDto;
import com.example.realestate.dto.model.RealEstateDto;
import com.example.realestate.dto.model.RealEstateUpdateDto;
import com.example.realestate.service.PolygonService;
import com.example.realestate.service.RealEstateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * REST Controller that exposes endpoints to retrieve or update real estate data.
 */
@Slf4j
@RestController
@RequestMapping("/api/real-estate")
public class RealEstateController {

    private final RealEstateService realEstateService;

    public RealEstateController(RealEstateService realEstateService) {
        this.realEstateService = realEstateService;
    }

    /**
     * Existing endpoint that returns ALL matching RealEstateDto (full details).
     */
    @GetMapping
    public List<RealEstateDto> getRealEstate(RealEstateFilterDto filterDto) {
        log.info("GET /api/real-estate (all full) - filter: {}", filterDto);
        return realEstateService.getFilteredRealEstate(filterDto);
    }

    /**
     * Returns minimal real estate data for map usage.
     */
    @GetMapping("/map")
    public ResponseEntity<Map<String, List<RealEstateMapDto>>> getRealEstateMapData(
            RealEstateFilterDto filterDto,
            @RequestParam(value = "polygonId", required = false) String polygonId) {
        log.info("GET /api/real-estate/map - filter: {}, polygonId: {}", filterDto, polygonId);
        List<RealEstateMapDto> filteredData = realEstateService.getFilteredRealEstateMapData(filterDto);
        List<RealEstateMapDto> attachedData = polygonId != null
                ? realEstateService.getAttachedRealEstateMapDataByPolygonId(polygonId)
                : List.of();
        Map<String, List<RealEstateMapDto>> response = Map.of(
                "filtered", filteredData,
                "attached", attachedData
        );
        return ResponseEntity.ok(response);
    }

    /**
     * NEW: Returns a paginated list of RealEstateDto (full details).
     */
    @GetMapping("/paginated")
    public ResponseEntity<PaginatedResponseDto<RealEstateDto>> getRealEstatePaginated(
            RealEstateFilterDto filterDto,
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "50", name = "pageSize") int pageSize
    ) {
        log.info("GET /api/real-estate/paginated - page={}, pageSize={}, filter={}", page, pageSize, filterDto);
        Page<RealEstateDto> pageResult = realEstateService.getFilteredRealEstatePaginated(filterDto, page, pageSize);

        PaginatedResponseDto<RealEstateDto> response = new PaginatedResponseDto<>(
                pageResult.getContent(),
                page,
                pageSize,
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * The attached endpoint for reference (unchanged).
     */
    @GetMapping("/attached")
    public ResponseEntity<List<RealEstateDto>> getAttachedRealEstate(@RequestParam("ids") String ids) {
        log.info("GET /api/real-estate/attached with ids: {}", ids);
        List<String> idList = Arrays.asList(ids.split(","));
        List<RealEstateDto> attachedRecords = realEstateService.getRealEstateByIds(idList);
        return ResponseEntity.ok(attachedRecords);
    }

    // ----------------------------------------------------------------
    // NEW: Update the RealEstate with the given ID using RealEstateUpdateDto
    // ----------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<RealEstateDto> updateRealEstate(
            @PathVariable("id") String id,
            @RequestBody RealEstateUpdateDto updateDto
    ) {
        log.info("PUT /api/real-estate/{} -> {}", id, updateDto);
        RealEstateDto updated = realEstateService.update(id, updateDto);
        return ResponseEntity.ok(updated);
    }
}

package com.example.realestate.controller.model;

import com.example.realestate.dto.PaginatedResponseDto;
import com.example.realestate.dto.RealEstateDto;
import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.dto.RealEstateMapDto;
import com.example.realestate.service.RealEstateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * Existing endpoint that returns ALL matching RealEstateDto (full details),
     * without pagination. (Kept for backward-compatibility.)
     */
    @GetMapping
    public List<RealEstateDto> getRealEstate(RealEstateFilterDto filterDto) {
        log.info("GET /api/real-estate (all full) - filter: {}", filterDto);
        return realEstateService.getFilteredRealEstate(filterDto);
    }

    /**
     * NEW: Returns ALL matching real estate for the map,
     * but only minimal columns (RealEstateMapDto).
     * Example usage:
     * GET /api/real-estate/map?city=Provo&minPrice=200000
     */
    @GetMapping("/map")
    public ResponseEntity<List<RealEstateMapDto>> getRealEstateMapData(RealEstateFilterDto filterDto) {
        log.info("GET /api/real-estate/map - filter: {}", filterDto);
        List<RealEstateMapDto> data = realEstateService.getFilteredRealEstateMapData(filterDto);
        return ResponseEntity.ok(data);
    }

    /**
     * NEW: Returns a paginated list of RealEstateDto (full details),
     * but only the requested page (e.g., 100 items).
     * Example usage:
     * GET /api/real-estate/paginated?page=1&pageSize=100&city=Provo
     */
    @GetMapping("/paginated")
    public ResponseEntity<PaginatedResponseDto<RealEstateDto>> getRealEstatePaginated(
            RealEstateFilterDto filterDto,
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "50", name = "pageSize") int pageSize
    ) {
        log.info("GET /api/real-estate/paginated - page={}, pageSize={}, filter={}", page, pageSize, filterDto);

        // Use the new service method
        Page<RealEstateDto> pageResult =
                realEstateService.getFilteredRealEstatePaginated(filterDto, page, pageSize);

        PaginatedResponseDto<RealEstateDto> response = new PaginatedResponseDto<>(
                pageResult.getContent(),
                page,
                pageSize,
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    // The attached endpoint for reference (unchanged)
    @GetMapping("/attached")
    public ResponseEntity<List<RealEstateDto>> getAttachedRealEstate(@RequestParam("ids") String ids) {
        log.info("GET /api/real-estate/attached with ids: {}", ids);
        List<String> idList = Arrays.asList(ids.split(","));
        List<RealEstateDto> attachedRecords = realEstateService.getRealEstateByIds(idList);
        return ResponseEntity.ok(attachedRecords);
    }
}

package com.example.realestate.service;

import com.example.realestate.converters.model.RealEstateToRealEstateDtoConverter;
import com.example.realestate.dto.model.RealEstateDto;
import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.dto.RealEstateMapDto;
import com.example.realestate.model.RealEstate;
import com.example.realestate.repository.RealEstateRepository;
import com.example.realestate.specification.RealEstateSpecification;
import com.example.realestate.util.PriceParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RealEstateService implements CrudService<RealEstateDto, String> {

    private final RealEstateRepository realEstateRepository;
    private final RealEstateToRealEstateDtoConverter realEstateConverter;

    public RealEstateService(
            RealEstateRepository realEstateRepository,
            RealEstateToRealEstateDtoConverter realEstateConverter
    ) {
        this.realEstateRepository = realEstateRepository;
        this.realEstateConverter = realEstateConverter;
    }

    @Override
    public RealEstateDto create(RealEstateDto dto) {
        // (Placeholder - not relevant for this sample)
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public RealEstateDto getById(String id) {
        // (Placeholder - not relevant for this sample)
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<RealEstateDto> getAll() {
        // (Placeholder - not relevant for this sample)
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public RealEstateDto update(String id, RealEstateDto updatedDto) {
        // (Placeholder - not relevant for this sample)
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(String id) {
        // (Placeholder - not relevant for this sample)
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<RealEstateDto> getRealEstateByIds(List<String> ids) {
        log.info("Fetching real estate records for IDs: {}", ids);
        List<RealEstate> records = realEstateRepository.findAllByIdIn(ids);
        return records.stream()
                .map(realEstateConverter::convert)
                .collect(Collectors.toList());
    }

    // In RealEstateService.java, add this method:
    public List<RealEstate> getRealEstateEntitiesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return realEstateRepository.findAllById(ids);
    }


    /**
     * Our existing method that returns ALL filtered real estate
     * with full fields (RealEstateDto).
     * We still keep it for backward-compatibility (used in older code),
     * but going forward we prefer new specialized endpoints.
     */
    public List<RealEstateDto> getFilteredRealEstate(RealEstateFilterDto filterDto) {
        log.info("Filtering real estate (all) with filters: {}", filterDto);
        List<RealEstate> filtered = realEstateRepository.findAll(RealEstateSpecification.withFilters(filterDto));

        // Java-based price filtering (since listPrice is a string).
        if (filterDto.getMinPrice() != null) {
            filtered = filtered.stream()
                    .filter(re -> parsePriceSafe(re.getListPrice()) >= filterDto.getMinPrice())
                    .collect(Collectors.toList());
        }
        if (filterDto.getMaxPrice() != null) {
            filtered = filtered.stream()
                    .filter(re -> parsePriceSafe(re.getListPrice()) <= filterDto.getMaxPrice())
                    .collect(Collectors.toList());
        }

        List<RealEstateDto> result = filtered.stream()
                .map(realEstateConverter::convert)
                .collect(Collectors.toList());
        log.info("Found {} real estate records (full data).", result.size());
        return result;
    }

    /**
     * 1) New method: returns a PAGE of real estate (100 at a time, for example),
     * but with the full RealEstateDto fields (like your normal details).
     *
     * @param filterDto The same filter object
     * @param page      1-based page index from the caller
     * @param pageSize  how many records per page
     * @return a Page of RealEstateDto
     */
    public Page<RealEstateDto> getFilteredRealEstatePaginated(RealEstateFilterDto filterDto, int page, int pageSize) {
        // Convert user-friendly "page" (1-based) into Spring Data "pageIndex" (0-based).
        int pageIndex = page > 0 ? page - 1 : 0;

        // Build a PageRequest. (No sorting for now, you can add sorting if needed.)
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        // Get specification to filter by city, state, etc.
        var spec = RealEstateSpecification.withFilters(filterDto);

        // Retrieve a page of RealEstate objects.
        Page<RealEstate> pageResult = realEstateRepository.findAll(spec, pageable);

        // We still need to do the price filtering in memory for minPrice/maxPrice,
        // so let's map content to a list, filter by price, then re-wrap in a "Page" manually.
        List<RealEstate> filteredList = pageResult.getContent().stream()
                .filter(re -> {
                    double price = parsePriceSafe(re.getListPrice());
                    boolean aboveMin = (filterDto.getMinPrice() == null) || (price >= filterDto.getMinPrice());
                    boolean belowMax = (filterDto.getMaxPrice() == null) || (price <= filterDto.getMaxPrice());
                    return aboveMin && belowMax;
                })
                .toList();

        List<RealEstateDto> mappedDtos = filteredList.stream()
                .map(realEstateConverter::convert)
                .collect(Collectors.toList());

        // Because we filtered out some items by price,
        // we might have fewer results in "mappedDtos".
        // We'll create a new Page object for them:
        // totalElements is still from DB ignoring price filter
        return new PageImpl<>(
                mappedDtos,
                pageable,
                pageResult.getTotalElements() // totalElements is still from DB ignoring price filter
        );
    }

    /**
     * 2) New method: returns ALL real estate that match the filters,
     * but only minimal columns (RealEstateMapDto) for the map.
     *
     * @param filterDto filter conditions
     * @return a List of RealEstateMapDto (could be up to 100k!)
     */
    public List<RealEstateMapDto> getFilteredRealEstateMapData(RealEstateFilterDto filterDto) {
        log.info("Filtering real estate for map with filters: {}", filterDto);
        // Do the same approach as before, but only map minimal fields.

        // Step 1: Get all that match (excluding price filtering in DB).
        var spec = RealEstateSpecification.withFilters(filterDto);
        List<RealEstate> entities = realEstateRepository.findAll(spec);

        // Step 2: Price filter in memory
        entities = entities.stream()
                .filter(re -> {
                    double price = parsePriceSafe(re.getListPrice());
                    boolean aboveMin = (filterDto.getMinPrice() == null) || (price >= filterDto.getMinPrice());
                    boolean belowMax = (filterDto.getMaxPrice() == null) || (price <= filterDto.getMaxPrice());
                    return aboveMin && belowMax;
                })
                .toList();

        // Step 3: Convert to minimal RealEstateMapDto
        List<RealEstateMapDto> result = entities.stream()
                .map(re -> {
                    // Convert BigDecimals to Double
                    Double lat = (re.getLatitude() != null) ? re.getLatitude().doubleValue() : null;
                    Double lng = (re.getLongitude() != null) ? re.getLongitude().doubleValue() : null;
                    return new RealEstateMapDto(
                            re.getId(),
                            lat,
                            lng,
                            re.getCity(),
                            re.getState(),
                            re.getStatus()
                    );
                })
                .collect(Collectors.toList());

        log.info("Found {} real estate records (map data).", result.size());
        return result;
    }

    /**
     * Safely parse listPrice (a string like "$400,000") to double.
     * Return 0.0 if any error or blank.
     */
    private double parsePriceSafe(String priceStr) {
        try {
            return PriceParser.parsePrice(priceStr);
        } catch (ParseException e) {
            return 0.0; // or handle differently
        }
    }
}

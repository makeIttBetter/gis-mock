package com.example.realestate.service;

import com.example.realestate.converters.model.RealEstatePartialUpdateConverter;
import com.example.realestate.converters.model.RealEstateToRealEstateDtoConverter;
import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.dto.RealEstateMapDto;
import com.example.realestate.dto.model.RealEstateDto;
import com.example.realestate.dto.model.RealEstateUpdateDto;
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
    private final RealEstatePartialUpdateConverter realEstatePartialUpdateConverter; // << NEW

    public RealEstateService(
            RealEstateRepository realEstateRepository,
            RealEstateToRealEstateDtoConverter realEstateConverter,
            RealEstatePartialUpdateConverter realEstatePartialUpdateConverter
    ) {
        this.realEstateRepository = realEstateRepository;
        this.realEstateConverter = realEstateConverter;
        this.realEstatePartialUpdateConverter = realEstatePartialUpdateConverter;
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

    /**
     * NEW: Implement partial update using RealEstateUpdateDto.
     */
    public RealEstateDto update(String id, RealEstateUpdateDto updateDto) {
        log.info("RealEstateService.update id={}, updateDto={}", id, updateDto);

        // 1) Find existing record
        RealEstate existing = realEstateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No RealEstate found with ID " + id));

        // 2) Partially update fields
        realEstatePartialUpdateConverter.updateEntity(existing, updateDto);

        // 3) Rebuild full address if the user changed any address/city/state/zip
        existing.setFullAddress(buildFullAddress(existing));

        // 4) Save
        RealEstate saved = realEstateRepository.save(existing);

        // 5) Convert to RealEstateDto
        return realEstateConverter.convert(saved);
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
     * Return ALL matching RealEstate as minimal map data (RealEstateMapDto).
     * Updated to include soldTerms, soldPrice, taxId, address, zip.
     */
    public List<RealEstateMapDto> getFilteredRealEstateMapData(RealEstateFilterDto filterDto) {
        log.info("Filtering real estate for map with filters: {}", filterDto);
        var spec = RealEstateSpecification.withFilters(filterDto);
        List<RealEstate> entities = realEstateRepository.findAll(spec);

        // Price filter in memory:
        entities = entities.stream()
                .filter(re -> {
                    double price = parsePriceSafe(re.getListPrice());
                    boolean aboveMin = (filterDto.getMinPrice() == null) || (price >= filterDto.getMinPrice());
                    boolean belowMax = (filterDto.getMaxPrice() == null) || (price <= filterDto.getMaxPrice());
                    return aboveMin && belowMax;
                })
                .toList();

        // Convert to RealEstateMapDto
        List<RealEstateMapDto> result = entities.stream()
                .map(this::toMapDto)
                .collect(Collectors.toList());

        log.info("Found {} real estate records (map data).", result.size());
        return result;
    }

    /**
     * Helper method to build a full address from parts.
     */
    private String buildFullAddress(RealEstate e) {
        // We simply combine these fields with commas (adjust to your preference)
        StringBuilder sb = new StringBuilder();

        if (e.getAddress() != null && !e.getAddress().isBlank()) {
            sb.append(e.getAddress());
        }
        if (e.getCity() != null && !e.getCity().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(e.getCity());
        }
        if (e.getState() != null && !e.getState().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(e.getState());
        }
        if (e.getZip() != null && !e.getZip().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(e.getZip());
        }

        return sb.toString();
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

    /**
     * Convert a RealEstate entity to RealEstateMapDto with minimal fields
     * plus the extra ones we need for pop-up editing.
     */
    private RealEstateMapDto toMapDto(RealEstate re) {
        RealEstateMapDto dto = new RealEstateMapDto();
        dto.setId(re.getId());
        dto.setMlsNumber(re.getMlsNumber());
        dto.setSoldTerms(re.getSoldTerms());
        dto.setSoldPrice(re.getSoldPrice());
        dto.setTaxId(re.getTaxId());
        dto.setAddress(re.getAddress());
        dto.setCity(re.getCity());
        dto.setState(re.getState());
        dto.setZip(re.getZip());
        dto.setStatus(re.getStatus());

        // Convert BigDecimals to Double for lat/long
        if (re.getLatitude() != null) {
            dto.setLatitude(re.getLatitude().doubleValue());
        } else {
            dto.setLatitude(null);
        }
        if (re.getLongitude() != null) {
            dto.setLongitude(re.getLongitude().doubleValue());
        } else {
            dto.setLongitude(null);
        }
        return dto;
    }
}

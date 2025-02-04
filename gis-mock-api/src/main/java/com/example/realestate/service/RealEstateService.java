// File: src/main/java/com/example/realestate/service/RealEstateService.java
package com.example.realestate.service;

import com.example.realestate.converters.RealEstateToRealEstateDtoConverter;
import com.example.realestate.dto.RealEstateDto;
import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.model.RealEstate;
import com.example.realestate.repository.RealEstateRepository;
import com.example.realestate.specification.RealEstateSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RealEstateService implements CrudService<RealEstateDto, String> {

    private final RealEstateRepository realEstateRepository;
    private final RealEstateToRealEstateDtoConverter realEstateConverter;

    public RealEstateService(RealEstateRepository realEstateRepository,
                             RealEstateToRealEstateDtoConverter realEstateConverter) {
        this.realEstateRepository = realEstateRepository;
        this.realEstateConverter = realEstateConverter;
    }

    @Override
    public RealEstateDto create(RealEstateDto dto) {
        // (Implementation unchanged)
        // Create and save RealEstate, then convert and return dto.
        return null; // Placeholder – use your existing implementation.
    }

    @Override
    public RealEstateDto getById(String id) {
        // (Implementation unchanged)
        return null; // Placeholder – use your existing implementation.
    }

    @Override
    public List<RealEstateDto> getAll() {
        // (Implementation unchanged)
        return null; // Placeholder – use your existing implementation.
    }

    @Override
    public RealEstateDto update(String id, RealEstateDto updatedDto) {
        // (Implementation unchanged)
        return null; // Placeholder – use your existing implementation.
    }

    @Override
    public void delete(String id) {
        // (Implementation unchanged)
    }

    /**
     * Filters real estate records using DB queries where possible and then applies price range filtering in Java.
     */
    public List<RealEstateDto> getFilteredRealEstate(RealEstateFilterDto filterDto) {
        log.info("Filtering real estate with filters: {}", filterDto);

        // Apply price filtering (minPrice and maxPrice) in Java code since listPrice is stored as a string.
        List<RealEstate> filtered = realEstateRepository.findAll(RealEstateSpecification.withFilters(filterDto));
        if (filterDto.getMinPrice() != null) {
            filtered = filtered.stream()
                    .filter(re -> {
                        try {
                            double price = parsePrice(re.getListPrice());
                            return price >= filterDto.getMinPrice();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }
        if (filterDto.getMaxPrice() != null) {
            filtered = filtered.stream()
                    .filter(re -> {
                        try {
                            double price = parsePrice(re.getListPrice());
                            return price <= filterDto.getMaxPrice();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        List<RealEstateDto> result = filtered.stream()
                .map(realEstateConverter::convert)
                .collect(Collectors.toList());
        log.info("Found {} real estate records", result.size());
        return result;
    }

    /**
     * Helper method to parse a price string (e.g. "$398,000") into a numeric value.
     */
    private double parsePrice(String priceStr) throws ParseException {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0.0;
        }
        String cleaned = priceStr.replaceAll("[$,]", "");
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(cleaned);
        return number.doubleValue();
    }

    public List<RealEstateDto> getRealEstateByIds(List<String> ids) {
        log.info("Fetching real estate records for IDs: {}", ids);
        List<RealEstate> records = realEstateRepository.findAllByIdIn(ids);
        return records.stream()
                .map(realEstateConverter::convert)
                .collect(Collectors.toList());
    }
}

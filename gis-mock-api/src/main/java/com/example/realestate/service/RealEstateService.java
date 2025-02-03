// File: src/main/java/com/example/realestate/service/RealEstateService.java
package com.example.realestate.service;

import com.example.realestate.converters.RealEstateToRealEstateDtoConverter;
import com.example.realestate.dto.RealEstateDto;
import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.model.RealEstate;
import com.example.realestate.repository.RealEstateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        log.info("Creating real estate: {}", dto);
        RealEstate realEstate = RealEstate.builder()
                .mlsNumber(dto.getMlsNumber())
                // Assuming tax_id is not provided from the DTO; set to empty or adjust as needed.
                .taxId("")
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zip(dto.getZip())
                .status(dto.getStatus())
                .listPrice(dto.getListPrice())
                .latitude(new BigDecimal(dto.getLatitude()))
                .longitude(new BigDecimal(dto.getLongitude()))
                .build();
        RealEstate saved = realEstateRepository.save(realEstate);
        RealEstateDto result = realEstateConverter.convert(saved);
        log.info("Real estate created with ID: {}", result.getId());
        return result;
    }

    @Override
    public RealEstateDto getById(String id) {
        log.info("Finding real estate by ID: {}", id);
        RealEstate re = realEstateRepository.findById(id).orElse(null);
        if (re == null) {
            log.warn("Real estate not found for ID: {}", id);
            return null;
        }
        return realEstateConverter.convert(re);
    }

    @Override
    public List<RealEstateDto> getAll() {
        log.info("Fetching all real estate records");
        List<RealEstate> all = realEstateRepository.findAll();
        return all.stream()
                .map(realEstateConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public RealEstateDto update(String id, RealEstateDto updatedDto) {
        log.info("Updating real estate with ID: {}", id);
        RealEstate existing = realEstateRepository.findById(id).orElse(null);
        if (existing == null) {
            log.warn("Real estate not found for update with ID: {}", id);
            return null;
        }
        existing.setMlsNumber(updatedDto.getMlsNumber());
        existing.setAddress(updatedDto.getAddress());
        existing.setCity(updatedDto.getCity());
        existing.setState(updatedDto.getState());
        existing.setZip(updatedDto.getZip());
        existing.setStatus(updatedDto.getStatus());
        existing.setListPrice(updatedDto.getListPrice());
        existing.setLatitude(new BigDecimal(updatedDto.getLatitude()));
        existing.setLongitude(new BigDecimal(updatedDto.getLongitude()));
        RealEstate saved = realEstateRepository.save(existing);
        RealEstateDto result = realEstateConverter.convert(saved);
        log.info("Real estate updated with ID: {}", result.getId());
        return result;
    }

    @Override
    public void delete(String id) {
        log.info("Deleting real estate with ID: {}", id);
        realEstateRepository.deleteById(id);
    }

    /**
     * Additional method: Filtering real estate records by criteria.
     */
    public List<RealEstateDto> getFilteredRealEstate(RealEstateFilterDto filterDto) {
        log.info("Filtering real estate with filters: {}", filterDto);
        List<RealEstate> all = realEstateRepository.findAll();

        if (filterDto.getCity() != null && !filterDto.getCity().isEmpty()) {
            all = all.stream()
                    .filter(re -> re.getCity() != null && re.getCity().equalsIgnoreCase(filterDto.getCity()))
                    .collect(Collectors.toList());
        }
        if (filterDto.getState() != null && !filterDto.getState().isEmpty()) {
            all = all.stream()
                    .filter(re -> re.getState() != null && re.getState().equalsIgnoreCase(filterDto.getState()))
                    .collect(Collectors.toList());
        }
        if (filterDto.getStatus() != null && !filterDto.getStatus().isEmpty()) {
            all = all.stream()
                    .filter(re -> re.getStatus() != null && re.getStatus().equalsIgnoreCase(filterDto.getStatus()))
                    .collect(Collectors.toList());
        }
        if (filterDto.getMinPrice() != null) {
            all = all.stream()
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
            all = all.stream()
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
        List<RealEstateDto> result = all.stream()
                .map(realEstateConverter::convert)
                .collect(Collectors.toList());
        log.info("Found {} real estate records", result.size());
        return result;
    }

    /**
     * Helper method to convert a listPrice string (e.g. "$398,000") to a numeric value.
     */
    private double parsePrice(String priceStr) throws ParseException {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0.0;
        }
        // Remove dollar signs and commas
        String cleaned = priceStr.replaceAll("[$,]", "");
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(cleaned);
        return number.doubleValue();
    }
}

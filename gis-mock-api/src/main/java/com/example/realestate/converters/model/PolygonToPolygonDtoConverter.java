package com.example.realestate.converters.model;

import com.example.realestate.dto.CoordinateDto;
import com.example.realestate.dto.PolygonDto;
import com.example.realestate.model.Polygon;
import com.example.realestate.model.PolygonRealEstate;
import com.example.realestate.repository.PolygonRealEstateRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PolygonToPolygonDtoConverter implements Converter<Polygon, PolygonDto> {

    private final ObjectMapper objectMapper;
    private final PolygonRealEstateRepository linkRepository;

    @Override
    public PolygonDto convert(Polygon source) {
        PolygonDto dto = new PolygonDto();
        dto.setId(source.getId());
        dto.setName(source.getName());

        // Convert "coordinates" JSON to a list of CoordinateDto
        List<CoordinateDto> coords;
        try {
            coords = objectMapper.readValue(
                    source.getCoordinates(),
                    new TypeReference<List<CoordinateDto>>() {
                    }
            );
        } catch (Exception e) {
            coords = List.of();
        }
        dto.setCoordinates(coords);

        // Convert dateCreated, dateUpdated to a string
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (source.getCreatedAt() != null) {
            dto.setDateCreated(source.getCreatedAt().format(fmt));
        }
        if (source.getUpdatedAt() != null) {
            dto.setDateUpdated(source.getUpdatedAt().format(fmt));
        }

        // Instead of reading source.getRealEstateIds() from JSON for the final list,
        // we read from the link table for "real" list of IDs:
        List<String> linkedIds = linkRepository.findByPolygonId(source.getId())
                .stream()
                .map(PolygonRealEstate::getRealEstateId)
                .collect(Collectors.toList());
        dto.setRealEstateObjects(linkedIds);

        return dto;
    }
}

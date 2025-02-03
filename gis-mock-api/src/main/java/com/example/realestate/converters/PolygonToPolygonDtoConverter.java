package com.example.realestate.converters;

import com.example.realestate.dto.CoordinateDto;
import com.example.realestate.dto.PolygonDto;
import com.example.realestate.model.Polygon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PolygonToPolygonDtoConverter implements Converter<Polygon, PolygonDto> {

    private final ObjectMapper objectMapper;

    public PolygonToPolygonDtoConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PolygonDto convert(Polygon source) {
        log.debug("Converting Polygon to PolygonDto: {}", source);
        PolygonDto dto = new PolygonDto();
        dto.setId(source.getId());
        dto.setName(source.getName());
        try {
            // Deserialize coordinates from JSON string to list of CoordinateDto
            List<?> coords = objectMapper.readValue(source.getCoordinates(), List.class);
            List<CoordinateDto> coordinateDtos = coords.stream().map(coordinate -> {
                CoordinateDto c = new CoordinateDto();
                if (coordinate instanceof java.util.Map<?, ?> map) {
                    c.setLat(((Number) map.get("lat")).doubleValue());
                    c.setLng(((Number) map.get("lng")).doubleValue());
                }
                return c;
            }).collect(Collectors.toList());
            dto.setCoordinates(coordinateDtos);

            // Deserialize real estate IDs
            List<String> reIds = objectMapper.readValue(source.getRealEstateIds(), List.class);
            dto.setRealEstateObjects(reIds);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (source.getCreatedAt() != null) {
                dto.setDateCreated(source.getCreatedAt().format(formatter));
            }
            if (source.getUpdatedAt() != null) {
                dto.setDateUpdated(source.getUpdatedAt().format(formatter));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting Polygon to PolygonDto", e);
        } finally {
            log.debug("Conversion complete");
        }
        return dto;
    }
}

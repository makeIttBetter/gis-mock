package com.example.realestate.converters;

import com.example.realestate.dto.PolygonCreateDto;
import com.example.realestate.model.Polygon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PolygonCreateDtoToPolygonConverter implements Converter<PolygonCreateDto, Polygon> {

    private final ObjectMapper objectMapper;

    public PolygonCreateDtoToPolygonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Polygon convert(PolygonCreateDto source) {
        log.debug("Converting PolygonCreateDto to Polygon: {}", source);
        try {
            String coordinatesJson = objectMapper.writeValueAsString(source.getCoordinates());
            String reIdsJson = objectMapper.writeValueAsString(source.getRealEstateIds());
            return Polygon.builder()
                    .name(source.getName())
                    .coordinates(coordinatesJson)
                    .realEstateIds(reIdsJson)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting PolygonCreateDto to Polygon", e);
        } finally {
            log.debug("Conversion complete");
        }
    }
}

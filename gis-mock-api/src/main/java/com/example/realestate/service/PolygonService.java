// File: src/main/java/com/example/realestate/service/PolygonService.java
package com.example.realestate.service;

import com.example.realestate.converters.PolygonCreateDtoToPolygonConverter;
import com.example.realestate.converters.PolygonToPolygonDtoConverter;
import com.example.realestate.dto.PolygonCreateDto;
import com.example.realestate.dto.PolygonDto;
import com.example.realestate.model.Polygon;
import com.example.realestate.repository.PolygonRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PolygonService implements CrudService<PolygonDto, String> {

    private final PolygonRepository polygonRepository;
    private final PolygonCreateDtoToPolygonConverter createConverter;
    private final PolygonToPolygonDtoConverter toDtoConverter;
    private final ObjectMapper objectMapper;
    private final ArcgisLayerService arcgisLayerService;

    @Value("${app.links.layers}")
    private String layersUrl;

    public PolygonService(PolygonRepository polygonRepository,
                          PolygonCreateDtoToPolygonConverter createConverter,
                          PolygonToPolygonDtoConverter toDtoConverter,
                          ObjectMapper objectMapper,
                          ArcgisLayerService arcgisLayerService) {
        this.polygonRepository = polygonRepository;
        this.createConverter = createConverter;
        this.toDtoConverter = toDtoConverter;
        this.objectMapper = objectMapper;
        this.arcgisLayerService = arcgisLayerService;
    }

    /**
     * Create a new polygon and, upon success, create a corresponding ArcGIS layer.
     */
    @Override
    public PolygonDto create(PolygonDto polygonDto) {
        log.info("Creating polygon: {}", polygonDto);
        // Create a temporary PolygonCreateDto from the incoming PolygonDto
        PolygonCreateDto createDto = new PolygonCreateDto();
        createDto.setName(polygonDto.getName());
        createDto.setCoordinates(polygonDto.getCoordinates());
        createDto.setRealEstateIds(polygonDto.getRealEstateObjects());
        Polygon polygon = createConverter.convert(createDto);
        Polygon saved = polygonRepository.save(polygon);

        // Construct the external data URL for the new layer.
        // This should point to your application’s endpoint that serves layer data.
        String dataUrl = layersUrl + saved.getId();

        // Create corresponding layer in ArcGIS using the polygon's name and the data URL.
        String arcgisLayerId = arcgisLayerService.createLayer(saved.getName(), dataUrl);
        saved.setArcgisLayerId(arcgisLayerId);
        saved = polygonRepository.save(saved);

        PolygonDto result = toDtoConverter.convert(saved);
        log.info("Polygon created with ID: {}", result.getId());
        return result;
    }

    @Override
    public PolygonDto getById(String id) {
        log.info("Finding polygon by ID: {}", id);
        Polygon polygon = polygonRepository.findById(id).orElse(null);
        if (polygon == null) {
            log.warn("Polygon not found for ID: {}", id);
            return null;
        }
        return toDtoConverter.convert(polygon);
    }

    @Override
    public List<PolygonDto> getAll() {
        log.info("Fetching all polygons");
        List<Polygon> polygons = polygonRepository.findAll();
        return polygons.stream()
                .map(toDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public PolygonDto update(String id, PolygonDto updatedDto) {
        log.info("Updating polygon with ID: {}", id);
        Polygon existing = polygonRepository.findById(id).orElse(null);
        if (existing == null) {
            log.warn("Polygon not found for update with ID: {}", id);
            return null;
        }
        existing.setName(updatedDto.getName());
        try {
            String coordinatesJson = objectMapper.writeValueAsString(updatedDto.getCoordinates());
            String reIdsJson = objectMapper.writeValueAsString(updatedDto.getRealEstateObjects());
            existing.setCoordinates(coordinatesJson);
            existing.setRealEstateIds(reIdsJson);
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON during polygon update", e);
            throw new RuntimeException(e);
        }
        Polygon saved = polygonRepository.save(existing);
        PolygonDto result = toDtoConverter.convert(saved);
        log.info("Polygon updated with ID: {}", result.getId());
        return result;
    }

    /**
     * Deletes the polygon and also deletes its corresponding ArcGIS layer.
     */
    @Override
    public void delete(String id) {
        log.info("Deleting polygon with ID: {}", id);
        Polygon polygon = polygonRepository.findById(id).orElse(null);
        if (polygon != null && polygon.getArcgisLayerId() != null) {
            arcgisLayerService.deleteLayer(polygon.getArcgisLayerId());
        }
        polygonRepository.deleteById(id);
    }

    // Overloaded create method if needed.
    public PolygonDto create(PolygonCreateDto polygonCreateDto) {
        log.info("Creating polygon (overloaded) with: {}", polygonCreateDto);
        Polygon polygon = createConverter.convert(polygonCreateDto);
        Polygon saved = polygonRepository.save(polygon);
        String dataUrl = layersUrl + saved.getId();
        String arcgisLayerId = arcgisLayerService.createLayer(saved.getName(), dataUrl);
        saved.setArcgisLayerId(arcgisLayerId);
        saved = polygonRepository.save(saved);
        PolygonDto result = toDtoConverter.convert(saved);
        log.info("Polygon created with ID: {}", result.getId());
        return result;
    }
}

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

    @Override
    public PolygonDto create(PolygonDto polygonDto) {
        log.info("Creating polygon from PolygonDto: {}", polygonDto);
        // We'll adapt the polygonDto -> PolygonCreateDto
        PolygonCreateDto createDto = new PolygonCreateDto();
        createDto.setName(polygonDto.getName());
        createDto.setCoordinates(polygonDto.getCoordinates());
        createDto.setRealEstateIds(polygonDto.getRealEstateObjects());

        return create(createDto); // Just reuse the overload below
    }

    /**
     * Overloaded create method that takes a PolygonCreateDto directly.
     * This is used by the REST controller and by create(PolygonDto).
     */
    public PolygonDto create(PolygonCreateDto polygonCreateDto) {
        log.info("Creating polygon (overloaded) with: {}", polygonCreateDto);

        // 1) Convert to entity and save (so we have an ID in the DB).
        Polygon polygon = createConverter.convert(polygonCreateDto);
        Polygon saved = polygonRepository.save(polygon);

        // 2) Create the ArcGIS dataset (as before). We'll call it "dataset layer".
        //    The dataUrl points to your openApi layer:  e.g.  https://yourdomain/openApi/layers/{polygonId}
//        String dataUrl = layersUrl + saved.getId();
        String dataUrl = "https://mytestapp.online/api/gis/mock-dots";
        String arcgisLayerId = arcgisLayerService.createLayer(saved.getName() + " (DataSet)", dataUrl);
        saved.setArcgisLayerId(arcgisLayerId);

        // 3) Create the ArcGIS polygon layer using the mock link
        //    (per REQUIREMENT #1).
        String polygonDataUrl = "https://mytestapp.online/api/gis/mock-polygon";
        String arcgisPolygonId = arcgisLayerService.createLayer(saved.getName() + " (Polygon)", polygonDataUrl);
        saved.setArcgisPolygonId(arcgisPolygonId);

        // 4) Save again to store these two new IDs
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
        // Per REQUIREMENT #2: Do NOT update the polygon in ArcGIS; no call to arcgisLayerService here.
        Polygon saved = polygonRepository.save(existing);
        PolygonDto result = toDtoConverter.convert(saved);
        log.info("Polygon updated with ID: {}", result.getId());
        return result;
    }

    @Override
    public void delete(String id) {
        log.info("Deleting polygon with ID: {}", id);
        Polygon polygon = polygonRepository.findById(id).orElse(null);
        if (polygon != null) {
            // REQUIREMENT #3: Delete both the dataset and the polygon layer in ArcGIS
            if (polygon.getArcgisLayerId() != null) {
                arcgisLayerService.deleteLayer(polygon.getArcgisLayerId());
            }
            if (polygon.getArcgisPolygonId() != null) {
                arcgisLayerService.deleteLayer(polygon.getArcgisPolygonId());
            }
        }
        polygonRepository.deleteById(id);
    }
}

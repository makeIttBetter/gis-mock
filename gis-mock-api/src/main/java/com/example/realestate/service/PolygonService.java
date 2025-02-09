package com.example.realestate.service;

import com.example.realestate.converters.model.PolygonCreateDtoToPolygonConverter;
import com.example.realestate.converters.model.PolygonToPolygonDtoConverter;
import com.example.realestate.dto.PolygonCreateDto;
import com.example.realestate.dto.PolygonDto;
import com.example.realestate.model.Polygon;
import com.example.realestate.model.PolygonRealEstate;
import com.example.realestate.repository.PolygonRealEstateRepository;
import com.example.realestate.repository.PolygonRepository;
import com.example.realestate.service.auth.SecurityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
// By default, let read-only queries happen without overriding. We'll explicitly mark "create, update, delete" with @Transactional(readOnly = false)
public class PolygonService implements CrudService<PolygonDto, String> {

    private final PolygonRepository polygonRepository;
    private final PolygonRealEstateRepository polygonRealEstateRepository;
    private final PolygonCreateDtoToPolygonConverter createConverter;
    private final PolygonToPolygonDtoConverter toDtoConverter;
    private final ObjectMapper objectMapper;
    private final ArcgisLayerService arcgisLayerService;
    private final SecurityService securityService;

    @Value("${app.links.layers}")
    private String layersUrl;

    public PolygonService(PolygonRepository polygonRepository,
                          PolygonRealEstateRepository polygonRealEstateRepository,
                          PolygonCreateDtoToPolygonConverter createConverter,
                          PolygonToPolygonDtoConverter toDtoConverter,
                          ObjectMapper objectMapper,
                          ArcgisLayerService arcgisLayerService,
                          SecurityService securityService
    ) {
        this.polygonRepository = polygonRepository;
        this.polygonRealEstateRepository = polygonRealEstateRepository;
        this.createConverter = createConverter;
        this.toDtoConverter = toDtoConverter;
        this.objectMapper = objectMapper;
        this.arcgisLayerService = arcgisLayerService;
        this.securityService = securityService;
    }

    @Override
    @Transactional(readOnly = false)
    public PolygonDto create(PolygonDto polygonDto) {
        log.info("Creating polygon from PolygonDto: {}", polygonDto);
        // Convert polygonDto -> PolygonCreateDto
        PolygonCreateDto createDto = new PolygonCreateDto();
        createDto.setName(polygonDto.getName());
        createDto.setCoordinates(polygonDto.getCoordinates());
        createDto.setRealEstateIds(polygonDto.getRealEstateObjects());

        return create(createDto);
    }

    /**
     * Overloaded create method that takes PolygonCreateDto directly.
     */
    @Transactional(readOnly = false)
    public PolygonDto create(PolygonCreateDto polygonCreateDto) {
        log.info("Creating polygon with: {}", polygonCreateDto);

        // --- Ensure we have the user ID
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

        // 1) Convert to entity
        Polygon polygon = createConverter.convert(polygonCreateDto);

        // 1b) Set the current user's ID
        polygon.setUserId(currentUserId);

        // 2) Save so we get an ID
        Polygon saved = polygonRepository.save(polygon);

        // 3) Create ArcGIS layers and set arcgis IDs
        String dataUrl = "https://mytestapp.online/api/gis/mock-dots";
        String arcgisLayerId = arcgisLayerService.createLayer(saved.getName() + " (DataSet)", dataUrl);
        saved.setArcgisLayerId(arcgisLayerId);

        String polygonDataUrl = "https://mytestapp.online/api/gis/mock-polygon";
        String arcgisPolygonId = arcgisLayerService.createLayer(saved.getName() + " (Polygon)", polygonDataUrl);
        saved.setArcgisPolygonId(arcgisPolygonId);

        saved = polygonRepository.save(saved);

        // 4) Store real estate IDs in JSON field & link table
        List<String> reIds = polygonCreateDto.getRealEstateIds();
        if (reIds != null) {
            try {
                String reIdsJson = objectMapper.writeValueAsString(reIds);
                saved.setRealEstateIds(reIdsJson);
                saved = polygonRepository.save(saved);
            } catch (JsonProcessingException e) {
                log.error("Error JSONifying realEstateIds", e);
            }
            // Add the link rows
            addPolygonRealEstateLinks(saved.getId(), reIds);
        }

        // 5) Convert to DTO
        return toDtoConverter.convert(saved);
    }

    @Override
    public PolygonDto getById(String id) {
        log.info("Finding polygon by ID: {}", id);
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

        // Only fetch the polygon if it belongs to the user
        Polygon polygon = polygonRepository.findByIdAndUserId(id, currentUserId).orElse(null);
        if (polygon == null) {
            log.warn("Polygon not found or does not belong to user: {}", id);
            return null;
        }
        return toDtoConverter.convert(polygon);
    }

    @Override
    public List<PolygonDto> getAll() {
        log.info("Fetching all polygons for current user");
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

        // Fetch only polygons for the current user
        List<Polygon> polygons = polygonRepository.findAllByUserId(currentUserId);
        return polygons.stream()
                .map(toDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = false)
    public PolygonDto update(String id, PolygonDto updatedDto) {
        log.info("Updating polygon with ID: {}", id);
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

        // Only fetch polygon if it belongs to current user
        Polygon existing = polygonRepository.findByIdAndUserId(id, currentUserId).orElse(null);
        if (existing == null) {
            log.warn("Polygon not found or not owned by user: {}", id);
            return null;
        }
        // Basic fields
        existing.setName(updatedDto.getName());
        try {
            String coordsJson = objectMapper.writeValueAsString(updatedDto.getCoordinates());
            existing.setCoordinates(coordsJson);
        } catch (JsonProcessingException e) {
            log.error("Error writing coords JSON", e);
        }

        existing = polygonRepository.save(existing);

        // 2) Update real estate links
        removePolygonRealEstateLinks(id);
        List<String> newReIds = updatedDto.getRealEstateObjects();
        if (newReIds != null) {
            try {
                existing.setRealEstateIds(objectMapper.writeValueAsString(newReIds));
                existing = polygonRepository.save(existing);
            } catch (JsonProcessingException e) {
                log.error("Error writing realEstateIds JSON", e);
            }
            addPolygonRealEstateLinks(id, newReIds);
        }

        return toDtoConverter.convert(existing);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String id) {
        log.info("Deleting polygon with ID: {}", id);
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

        // Only fetch polygon if it belongs to the current user
        Polygon polygon = polygonRepository.findByIdAndUserId(id, currentUserId).orElse(null);
        if (polygon == null) {
            log.warn("Polygon not found or not owned by user: {}", id);
            return;
        }

        // ArcGIS delete calls
        if (polygon.getArcgisLayerId() != null) {
            arcgisLayerService.deleteLayer(polygon.getArcgisLayerId());
        }
        if (polygon.getArcgisPolygonId() != null) {
            arcgisLayerService.deleteLayer(polygon.getArcgisPolygonId());
        }

        // Remove from link table
        removePolygonRealEstateLinks(id);

        // Finally remove polygon itself
        polygonRepository.deleteById(id);
    }

    // -----------------------------------------------------------------------
    // Helper methods to keep code simpler
    // -----------------------------------------------------------------------

    private void removePolygonRealEstateLinks(String polygonId) {
        log.info("Removing PolygonRealEstate links for polygonId={}", polygonId);
        polygonRealEstateRepository.deleteByPolygonId(polygonId);
    }

    private void addPolygonRealEstateLinks(String polygonId, List<String> reIds) {
        log.info("Adding PolygonRealEstate links for polygonId={}, reIds={}", polygonId, reIds);
        // Optionally filter duplicates
        java.util.Set<String> uniqueIds = new java.util.HashSet<>(reIds);

        for (String reId : uniqueIds) {
            PolygonRealEstate link = PolygonRealEstate.builder()
                    .polygonId(polygonId)
                    .realEstateId(reId)
                    .build();
            polygonRealEstateRepository.save(link);
        }
    }
}

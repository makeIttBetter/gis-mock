package com.example.realestate.service;

import com.example.realestate.converters.model.PolygonCreateDtoToPolygonConverter;
import com.example.realestate.converters.model.PolygonToPolygonDtoConverter;
import com.example.realestate.dto.model.PolygonCreateDto;
import com.example.realestate.dto.model.PolygonDto;
import com.example.realestate.model.Polygon;
import com.example.realestate.model.PolygonRealEstate;
import com.example.realestate.model.RealEstate;
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
public class PolygonService implements CrudService<PolygonDto, String> {

    private final PolygonRepository polygonRepository;
    private final PolygonRealEstateRepository polygonRealEstateRepository;
    private final RealEstateService realEstateService;
    private final PolygonCreateDtoToPolygonConverter createConverter;
    private final PolygonToPolygonDtoConverter toDtoConverter;
    private final ObjectMapper objectMapper;
    private final ArcgisLayerService arcgisLayerService;
    private final SecurityService securityService;

    @Value("${app.links.layers}")
    private String layersUrl;

    public PolygonService(
            PolygonRepository polygonRepository,
            PolygonRealEstateRepository polygonRealEstateRepository,
            RealEstateService realEstateService,
            PolygonCreateDtoToPolygonConverter createConverter,
            PolygonToPolygonDtoConverter toDtoConverter,
            ObjectMapper objectMapper,
            ArcgisLayerService arcgisLayerService,
            SecurityService securityService
    ) {
        this.polygonRepository = polygonRepository;
        this.polygonRealEstateRepository = polygonRealEstateRepository;
        this.realEstateService = realEstateService;
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

        // 1) Ensure we have the user ID
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

        // 2) Convert to entity
        Polygon polygon = createConverter.convert(polygonCreateDto);
        polygon.setUserId(currentUserId);

        // 3) Save so we get an ID
        Polygon saved = polygonRepository.save(polygon);

        // 4) Create ArcGIS layers and set arcgis IDs
        String dataUrl = "https://mytestapp.online/api/gis/mock-dots";
        String arcgisLayerId = arcgisLayerService.createLayer(saved.getName() + " (DataSet)", dataUrl);
        saved.setArcgisLayerId(arcgisLayerId);

        String polygonDataUrl = "https://mytestapp.online/api/gis/mock-polygon";
        String arcgisPolygonId = arcgisLayerService.createLayer(saved.getName() + " (Polygon)", polygonDataUrl);
        saved.setArcgisPolygonId(arcgisPolygonId);

        saved = polygonRepository.save(saved);

        // 5) Store real estate IDs in JSON field & link table
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

        // 6) Convert to DTO
        return toDtoConverter.convert(saved);
    }

    @Override
    public PolygonDto getById(String id) {
        log.info("Finding polygon by ID: {}", id);
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

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

        // 1) Update basic fields
        existing.setName(updatedDto.getName());
        try {
            String coordsJson = objectMapper.writeValueAsString(updatedDto.getCoordinates());
            existing.setCoordinates(coordsJson);
        } catch (JsonProcessingException e) {
            log.error("Error writing coords JSON", e);
        }

        // 2) Update ArcGIS layer names if polygon name changed
        renameArcGisLayers(existing, updatedDto.getName());

        existing = polygonRepository.save(existing);

        // 3) Update real estate links
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
    @Transactional
    public void delete(String id) {
        log.info("Deleting polygon with ID: {}", id);
        String currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found.");
        }

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

//        // Remove from link table
//        removePolygonRealEstateLinks(id);

        // Finally remove polygon itself
        polygonRepository.deleteById(id);
    }

    /**
     * Finds all RealEstate entities attached to a given polygon ID.
     */
    public List<RealEstate> findAttachedRealEstates(String polygonId) {
        log.info("Finding real estates for polygon ID: {}", polygonId);
        List<PolygonRealEstate> links = polygonRealEstateRepository.findByPolygonId(polygonId);
        List<String> realEstateIds = links.stream()
                .map(PolygonRealEstate::getRealEstateId)
                .toList();

        return realEstateService.getRealEstateEntitiesByIds(realEstateIds);
    }

    // ----------------------------------------------------------------
    // HELPER METHODS (private)
    // ----------------------------------------------------------------

    private void removePolygonRealEstateLinks(String polygonId) {
        log.info("Removing PolygonRealEstate links for polygonId={}", polygonId);
        polygonRealEstateRepository.deleteByPolygonId(polygonId);
    }

    private void addPolygonRealEstateLinks(String polygonId, List<String> reIds) {
        log.info("Adding PolygonRealEstate links for polygonId={}, reIds={}", polygonId, reIds);
        java.util.Set<String> uniqueIds = new java.util.HashSet<>(reIds);
        for (String reId : uniqueIds) {
            PolygonRealEstate link = PolygonRealEstate.builder()
                    .polygonId(polygonId)
                    .realEstateId(reId)
                    .build();
            polygonRealEstateRepository.save(link);
        }
    }

    /**
     * ADDED: Renames the ArcGIS layers (dataset + polygon) to reflect the new polygon name.
     * We add suffixes ("(DataSet)" and "(Polygon)") to differentiate them.
     */
    private void renameArcGisLayers(Polygon polygon, String newName) {
        // If the polygon has no ArcGIS IDs, skip
        if (polygon.getArcgisLayerId() != null) {
            arcgisLayerService.updateLayerName(
                    polygon.getArcgisLayerId(),
                    newName + " (DataSet)"
            );
        }
        if (polygon.getArcgisPolygonId() != null) {
            arcgisLayerService.updateLayerName(
                    polygon.getArcgisPolygonId(),
                    newName + " (Polygon)"
            );
        }
    }
}

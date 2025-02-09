package com.example.realestate.repository;

import com.example.realestate.model.PolygonRealEstate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for the link table between Polygon and RealEstate.
 */
@Repository
public interface PolygonRealEstateRepository extends JpaRepository<PolygonRealEstate, String> {

    // Find all link rows for a particular polygon
    java.util.List<PolygonRealEstate> findByPolygonId(String polygonId);

    // Find all link rows for a particular realEstateId
    java.util.List<PolygonRealEstate> findByRealEstateId(String realEstateId);

    /**
     * Deletes all link rows for a particular polygon.
     * We mark it as @Modifying and @Transactional so it actually issues the DELETE in one transaction.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PolygonRealEstate pr WHERE pr.polygonId = :polygonId")
    void deleteByPolygonId(String polygonId);
}

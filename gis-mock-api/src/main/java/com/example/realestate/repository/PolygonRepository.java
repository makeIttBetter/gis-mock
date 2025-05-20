package com.example.realestate.repository;

import com.example.realestate.model.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolygonRepository extends JpaRepository<Polygon, String> {

    /**
     * Find all polygons for a particular user.
     */
    List<Polygon> findAllByUserId(String userId);

    /**
     * Find a specific polygon (by ID) that belongs to a particular user.
     */
    Optional<Polygon> findByIdAndUserId(String id, String userId);

    List<Polygon> findAllByUserIdAndIdIn(String userId, List<String> ids);
}

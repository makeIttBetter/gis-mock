package com.example.realestate.repository;

import com.example.realestate.model.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolygonRepository extends JpaRepository<Polygon, String> {
}

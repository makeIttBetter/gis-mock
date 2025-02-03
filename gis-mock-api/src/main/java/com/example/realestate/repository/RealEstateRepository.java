package com.example.realestate.repository;

import com.example.realestate.model.RealEstate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RealEstateRepository extends JpaRepository<RealEstate, String> {
    // Method to find an existing record by its MLS Number
    Optional<RealEstate> findByMlsNumber(String mlsNumber);
}

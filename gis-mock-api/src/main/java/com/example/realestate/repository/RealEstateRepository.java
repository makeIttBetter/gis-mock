package com.example.realestate.repository;

import com.example.realestate.model.RealEstate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RealEstateRepository extends JpaRepository<RealEstate, String>, JpaSpecificationExecutor<RealEstate> {
    // Method to find an existing record by its MLS Number
    Optional<RealEstate> findByMlsNumber(String mlsNumber);

    // Retrieve all real estate records whose IDs are in the given list.
    List<RealEstate> findAllByIdIn(List<String> ids);
}

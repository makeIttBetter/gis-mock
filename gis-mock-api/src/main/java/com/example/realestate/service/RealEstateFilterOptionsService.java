// File: src/main/java/com/example/realestate/service/RealEstateFilterOptionsService.java
package com.example.realestate.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RealEstateFilterOptionsService {

    @PersistenceContext
    private EntityManager entityManager;

    // Only allow filtering on these fields
    private final Set<String> allowedFields = Set.of("city", "state", "status", "propertyType", "style");

    public List<String> getDistinctValues(String fieldName) {
        if (!allowedFields.contains(fieldName)) {
            throw new IllegalArgumentException("Invalid field name for filtering: " + fieldName);
        }
        // Build a JPQL query that selects distinct non-null values for the given field
        String jpql = "SELECT DISTINCT r." + fieldName +
                " FROM RealEstate r WHERE r." + fieldName + " IS NOT NULL ORDER BY r." + fieldName;
        TypedQuery<String> query = entityManager.createQuery(jpql, String.class);
        return query.getResultList();
    }
}

// File: src/main/java/com/example/realestate/specification/RealEstateSpecification.java
package com.example.realestate.specification;

import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.model.RealEstate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

public class RealEstateSpecification {

    public static Specification<RealEstate> withFilters(RealEstateFilterDto filter) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            
            if (filter.getCity() != null && !filter.getCity().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("city")), filter.getCity().toLowerCase()));
            }
            if (filter.getState() != null && !filter.getState().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("state")), filter.getState().toLowerCase()));
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("status")), filter.getStatus().toLowerCase()));
            }
            if (filter.getIds() != null && !filter.getIds().isBlank()) {
                String[] idsArray = filter.getIds().split(",");
                predicate = cb.and(predicate, root.get("id").in(Arrays.asList(idsArray)));
            }
            if (filter.getAddress() != null && !filter.getAddress().isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("address")), "%" + filter.getAddress().toLowerCase() + "%"));
            }
            if (filter.getZipcode() != null && !filter.getZipcode().isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("zip"), "%" + filter.getZipcode() + "%"));
            }
            
            return predicate;
        };
    }
}

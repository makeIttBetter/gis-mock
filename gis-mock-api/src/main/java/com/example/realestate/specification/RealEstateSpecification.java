package com.example.realestate.specification;

import com.example.realestate.dto.RealEstateFilterDto;
import com.example.realestate.model.RealEstate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * A specification builder for RealEstate. Uses small helper methods
 * to keep each filter condition easy to read and maintain (SRP).
 * Note: We handle "daysBackMin" and "daysBackMax" by simple date comparisons:
 * soldDate <= (today - daysBackMin), etc. This avoids custom SQL functions.
 */
public class RealEstateSpecification {

    /**
     * Main entry point to build a combined specification using all filters.
     */
    public static Specification<RealEstate> withFilters(RealEstateFilterDto filter) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // Each small method returns a new or updated Predicate.
            predicate = applyCityFilter(predicate, root, cb, filter);
            predicate = applyStateFilter(predicate, root, cb, filter);
            predicate = applyStatusFilter(predicate, root, cb, filter);
            predicate = applyIdsFilter(predicate, root, cb, filter);
            predicate = applyAddressFilter(predicate, root, cb, filter);
            predicate = applyZipcodeFilter(predicate, root, cb, filter);
            predicate = applyPropertyTypesFilter(predicate, root, cb, filter);
            predicate = applyStyleFilter(predicate, root, cb, filter);
            predicate = applyYearBuiltMinFilter(predicate, root, cb, filter);
            predicate = applyYearBuiltMaxFilter(predicate, root, cb, filter);
            predicate = applyGlaMinFilter(predicate, root, cb, filter);
            predicate = applyGlaMaxFilter(predicate, root, cb, filter);
            predicate = applyBasementSqFtMinFilter(predicate, root, cb, filter);
            predicate = applyBasementSqFtMaxFilter(predicate, root, cb, filter);
            predicate = applyBasementFinishedFilter(predicate, root, cb, filter);
            predicate = applyDaysBackMinFilter(predicate, root, cb, filter);
            predicate = applyDaysBackMaxFilter(predicate, root, cb, filter);

            // We do NOT handle minPrice / maxPrice here, since listPrice is string-based
            // and we do that filtering in RealEstateService after retrieving from DB.
            return predicate;
        };
    }

    // ------------------------------------------------------------------
    // Below are private helpers for each filter piece
    // ------------------------------------------------------------------

    private static Predicate applyCityFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getCity() != null && !filter.getCity().isBlank()) {
            return cb.and(predicate,
                    cb.equal(cb.lower(root.get("city")), filter.getCity().toLowerCase())
            );
        }
        return predicate;
    }

    private static Predicate applyStateFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getState() != null && !filter.getState().isBlank()) {
            return cb.and(predicate,
                    cb.equal(cb.lower(root.get("state")), filter.getState().toLowerCase())
            );
        }
        return predicate;
    }

    private static Predicate applyStatusFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            return cb.and(predicate,
                    cb.equal(cb.lower(root.get("status")), filter.getStatus().toLowerCase())
            );
        }
        return predicate;
    }

    private static Predicate applyIdsFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getIds() != null && !filter.getIds().isBlank()) {
            String[] idsArray = filter.getIds().split(",");
            List<String> idList = Arrays.asList(idsArray);
            return cb.and(predicate, root.get("id").in(idList));
        }
        return predicate;
    }

    private static Predicate applyAddressFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getAddress() != null && !filter.getAddress().isBlank()) {
            return cb.and(predicate,
                    cb.like(
                            cb.lower(root.get("address")),
                            "%" + filter.getAddress().toLowerCase() + "%"
                    )
            );
        }
        return predicate;
    }

    private static Predicate applyZipcodeFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getZipcode() != null && !filter.getZipcode().isBlank()) {
            return cb.and(predicate,
                    cb.like(root.get("zip"), "%" + filter.getZipcode() + "%")
            );
        }
        return predicate;
    }

    private static Predicate applyPropertyTypesFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getPropertyTypes() != null && !filter.getPropertyTypes().isBlank()) {
            String[] types = filter.getPropertyTypes().split(",");
            List<String> typeList = Arrays.asList(types);
            return cb.and(predicate, root.get("propertyType").in(typeList));
        }
        return predicate;
    }

    private static Predicate applyStyleFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getStyles() != null && !filter.getStyles().isBlank()) {
            String[] styles = filter.getStyles().split(",");
            List<String> styleList = Arrays.asList(styles);
            return cb.and(predicate, root.get("style").in(styleList));
        }
        return predicate;
    }

    private static Predicate applyYearBuiltMinFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getYearBuiltMin() != null) {
            return cb.and(predicate,
                    cb.greaterThanOrEqualTo(root.get("yearBuilt"), filter.getYearBuiltMin())
            );
        }
        return predicate;
    }

    private static Predicate applyYearBuiltMaxFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getYearBuiltMax() != null) {
            return cb.and(predicate,
                    cb.lessThanOrEqualTo(root.get("yearBuilt"), filter.getYearBuiltMax())
            );
        }
        return predicate;
    }

    private static Predicate applyGlaMinFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getGlaMin() != null) {
            return cb.and(predicate,
                    cb.greaterThanOrEqualTo(root.get("grossLivingAreaGla"), filter.getGlaMin())
            );
        }
        return predicate;
    }

    private static Predicate applyGlaMaxFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getGlaMax() != null) {
            return cb.and(predicate,
                    cb.lessThanOrEqualTo(root.get("grossLivingAreaGla"), filter.getGlaMax())
            );
        }
        return predicate;
    }

    private static Predicate applyBasementSqFtMinFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getBasementSqFtMin() != null) {
            return cb.and(predicate,
                    cb.greaterThanOrEqualTo(root.get("basementSquareFeet"), filter.getBasementSqFtMin())
            );
        }
        return predicate;
    }

    private static Predicate applyBasementSqFtMaxFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getBasementSqFtMax() != null) {
            return cb.and(predicate,
                    cb.lessThanOrEqualTo(root.get("basementSquareFeet"), filter.getBasementSqFtMax())
            );
        }
        return predicate;
    }

    private static Predicate applyBasementFinishedFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getBasementFinished() != null) {
            return cb.and(predicate,
                    cb.equal(root.get("basementFinished"), filter.getBasementFinished())
            );
        }
        return predicate;
    }

    /**
     * "daysBackMin" means we want records whose difference (now - soldDate) >= daysBackMin.
     * So, soldDate <= (now - daysBackMin). This is simpler than 'timestampdiff' usage.
     */
    private static Predicate applyDaysBackMinFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getDaysBackMin() != null) {
            LocalDate cutoff = LocalDate.now().minusDays(filter.getDaysBackMin());
            // Difference >= daysBackMin => soldDate <= cutoff
            return cb.and(predicate,
                    cb.lessThanOrEqualTo(root.get("soldDate"), cutoff)
            );
        }
        return predicate;
    }

    /**
     * "daysBackMax" means difference (now - soldDate) <= daysBackMax.
     * So, soldDate >= (now - daysBackMax).
     */
    private static Predicate applyDaysBackMaxFilter(
            Predicate predicate,
            Root<RealEstate> root,
            CriteriaBuilder cb,
            RealEstateFilterDto filter
    ) {
        if (filter.getDaysBackMax() != null) {
            LocalDate cutoff = LocalDate.now().minusDays(filter.getDaysBackMax());
            // Difference <= daysBackMax => soldDate >= cutoff
            return cb.and(predicate,
                    cb.greaterThanOrEqualTo(root.get("soldDate"), cutoff)
            );
        }
        return predicate;
    }
}

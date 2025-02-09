package com.example.realestate.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "polygon_real_estate",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"polygon_id", "real_estate_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PolygonRealEstate extends Model {
    // "id" is inherited from Model (which is your base class with a UUID + createdAt + updatedAt)

    @Column(name = "polygon_id", nullable = false)
    private String polygonId;

    @Column(name = "real_estate_id", nullable = false)
    private String realEstateId;
}

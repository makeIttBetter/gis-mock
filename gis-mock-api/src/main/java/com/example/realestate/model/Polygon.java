// File: src/main/java/com/example/realestate/model/Polygon.java
package com.example.realestate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "polygon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Polygon extends Model {

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(name = "coordinates", columnDefinition = "TEXT")
    private String coordinates;

    @Lob
    @Column(name = "real_estate_ids", columnDefinition = "TEXT")
    private String realEstateIds;

    // NEW: Field to store the corresponding ArcGIS layer ID
    @Column(name = "arcgis_layer_id")
    private String arcgisLayerId;
}

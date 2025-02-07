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

    // Field for the ArcGIS dataset/item ID
    @Column(name = "arcgis_layer_id")
    private String arcgisLayerId;

    // NEW: Field for the ArcGIS polygon layer ID
    // (added via the new Liquibase changeset).
    @Column(name = "arcgis_polygon_id")
    private String arcgisPolygonId;
}

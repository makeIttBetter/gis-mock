package com.example.realestate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "polygon")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Polygon extends Model {

    @Column(nullable = false)
    private String name;

    @Column(name = "coordinates", columnDefinition = "TEXT")
    private String coordinates;

    @Column(name = "real_estate_ids", columnDefinition = "TEXT")
    private String realEstateIds;

    @Column(name = "arcgis_layer_id")
    private String arcgisLayerId;

    @Column(name = "arcgis_polygon_id")
    private String arcgisPolygonId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "google_sheets_url")
    private String googleSheetsUrl;
}
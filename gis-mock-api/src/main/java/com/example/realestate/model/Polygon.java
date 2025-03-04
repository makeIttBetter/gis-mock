package com.example.realestate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    //    @Lob
    @Column(name = "coordinates", columnDefinition = "TEXT")
    private String coordinates;

    //    @Lob
    @Column(name = "real_estate_ids", columnDefinition = "TEXT")
    private String realEstateIds;

    @Column(name = "arcgis_layer_id")
    private String arcgisLayerId;

    @Column(name = "arcgis_polygon_id")
    private String arcgisPolygonId;

    /**
     * NEW: references the current user who owns this polygon.
     * We store the user’s UUID from the `users` table.
     */
    @Column(name = "user_id", nullable = false)
    private String userId;
}

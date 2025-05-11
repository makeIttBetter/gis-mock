package com.example.realestate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a role (e.g. "ADMIN", "USER") that can be assigned to a User.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends Model {

    @Column(unique = true, nullable = false)
    private String name;  // e.g. "ADMIN", "USER", "GUEST"

    private String description;
}

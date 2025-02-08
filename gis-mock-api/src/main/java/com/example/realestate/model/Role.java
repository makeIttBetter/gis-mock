package com.example.realestate.model;

import com.example.realestate.model.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Represents a role (e.g. "ADMIN", "USER") that can be assigned to a User.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends Model {

    @Column(unique = true, nullable = false)
    private String name;  // e.g. "ADMIN", "USER", "GUEST"

    private String description;
}

package com.example.realestate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user in our system for authentication.
 * Extends your base Model so it has ID, createdAt, updatedAt fields.
 */
@Entity
@Table(name = "users")  // "users" table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User extends Model {

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    /**
     * If you want to mark the auth provider, e.g. "google" or "none".
     */
    private String provider;

    /**
     * Many-to-many relationship with Role. For example, a user can have multiple roles.
     */
    @ManyToMany(fetch = FetchType.EAGER) // EAGER or LAZY is up to you
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}

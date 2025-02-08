package com.example.realestate.repository;

import com.example.realestate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String username);

    User findByEmail(String email);

    /**
     * For convenience, find by either username or email, ignoring case if desired.
     */
    default User findByUsernameOrEmail(String identifier) {
        // Simple approach: try username first, else email
        User user = findByUsername(identifier);
        if (user == null) {
            user = findByEmail(identifier);
        }
        return user;
    }

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

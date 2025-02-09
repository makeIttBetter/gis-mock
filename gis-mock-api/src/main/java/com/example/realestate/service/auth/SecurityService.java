package com.example.realestate.service.auth;

import com.example.realestate.model.User;
import com.example.realestate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Retrieves the current authenticated user from Spring Security.
 */
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    /**
     * Gets the ID of the currently authenticated user (from DB),
     * or null if none is authenticated.
     */
    public String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            // userDetails.getUsername() should be the user’s email
            User dbUser = userRepository.findByUsername(userDetails.getUsername());
            if (dbUser != null) {
                return dbUser.getId(); // the UUID in your “users” table
            }
        }
        return null; // or throw if you require auth
    }

}

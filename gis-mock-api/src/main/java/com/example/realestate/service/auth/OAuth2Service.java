package com.example.realestate.service.auth;

import com.example.realestate.dto.auth.UserAuthDTO;
import com.example.realestate.model.User;
import com.example.realestate.repository.UserRepository;
import com.example.realestate.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user creation/update when the user logs in via OAuth2 (e.g. Google).
 * Then returns a JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create or update a user from the given OAuth2 data,
     * then generate a JWT for them.
     */
    public String handleOAuthLogin(UserAuthDTO userAuthDTO) {
        // 1) See if there's an existing user by email
        User user = userRepository.findByUsername(userAuthDTO.getUsername());

        if (user == null) {
            // If no user, create a new one
            user = new User();
            user.setUsername(userAuthDTO.getUsername());
            user.setUsername(userAuthDTO.getUsername());
            // No real local password. If you want a random or empty, up to you
            user.setPassword(passwordEncoder.encode("oauth2user"));
            user.setProvider(userAuthDTO.getProvider());
            userRepository.save(user);
        } else {
            // If user exists, optionally update the provider
            if (user.getProvider() == null || user.getProvider().isEmpty()) {
                user.setProvider(userAuthDTO.getProvider());
            } else if (!user.getProvider().contains(userAuthDTO.getProvider())) {
                user.setProvider(user.getProvider() + "," + userAuthDTO.getProvider());
            }
            userRepository.save(user);
        }

        // 2) Generate JWT
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER") // or actual roles
                .build();

        return jwtUtil.generateToken(userDetails);
    }
}

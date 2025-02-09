package com.example.realestate.service.auth;

import com.example.realestate.dto.auth.LoginRequest;
import com.example.realestate.dto.auth.LoginResponse;
import com.example.realestate.exceptions.UserAlreadyExistsException;
import com.example.realestate.exceptions.UserNotFoundException;
import com.example.realestate.model.User;
import com.example.realestate.repository.UserRepository;
import com.example.realestate.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles sign-up, sign-in, verifying, and guest token logic
 * (mirroring the example you provided).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<String> signUp(String username, String email, String rawPassword) {
        log.info("Signing up user: {}", username);
        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already taken: " + username);
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setUsername(email);
        // encode the password
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setProvider("none");

        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully. Please check your email for verification instructions.");
    }

    public ResponseEntity<LoginResponse> signIn(LoginRequest loginRequest) {
        log.info("Signing in user: {}", loginRequest.getUsername());
        try {
            // Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // Now find the user
            User user = userRepository.findByUsername(loginRequest.getUsername());
            if (user == null) {
                throw new UserNotFoundException("User not found: " + loginRequest.getUsername());
            }
            log.info("User found: {}", user.getUsername());

            // Generate JWT
            String token = jwtUtil.generateToken(
                    org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword()) // though not used after
                            .authorities("ROLE_USER")      // or your actual roles
                            .build()
            );

            return ResponseEntity.ok(
                    new LoginResponse(
                            false,
                            token,
                            "Login successful"
                    )
            );
        } catch (AuthenticationException | UserNotFoundException e) {
            log.error("Login failed for {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Login failed: " + e.getMessage()));
        }
    }


}

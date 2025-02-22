package com.example.realestate.service.auth;

import com.example.realestate.dto.auth.LoginRequest;
import com.example.realestate.dto.auth.LoginResponse;
import com.example.realestate.exceptions.UserAlreadyExistsException;
import com.example.realestate.exceptions.UserNotFoundException;
import com.example.realestate.model.User;
import com.example.realestate.repository.UserRepository;
import com.example.realestate.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                            .password(user.getPassword())
                            .authorities("ROLE_USER")
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

    /**
     * Reads the jwtToken from the HttpOnly cookie and checks validity.
     * Also ensures the user from the token still exists in DB.
     */
    public boolean verifyTokenFromRequest(HttpServletRequest request) {
        String token = extractTokenFromCookies(request);
        if (token == null) {
            log.warn("No JWT token found in cookies.");
            return false;
        }

        // First check if the token is valid (not expired, etc.).
        boolean basicValid = jwtUtil.validateToken(token);
        if (!basicValid) {
            log.warn("JWT token is invalid or expired.");
            return false;
        }

        // Next: extract the username from the token and check if user exists
        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return false;
        }
        if (username == null) {
            log.warn("Token does not contain a valid username");
            return false;
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.warn("No user found in DB for username: {}", username);
            return false;
        }

        return true; // Passed all checks
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("jwtToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

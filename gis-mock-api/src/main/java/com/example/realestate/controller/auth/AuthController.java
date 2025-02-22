package com.example.realestate.controller.auth;

import com.example.realestate.dto.auth.LoginRequest;
import com.example.realestate.dto.auth.LoginResponse;
import com.example.realestate.dto.auth.TokenVerificationResponse;
import com.example.realestate.dto.auth.UserAuthDTO;
import com.example.realestate.service.auth.AuthService;
import com.example.realestate.validator.UserValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserValidator userValidator;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserAuthDTO userAuthDTO) {
        // Validate that user doesn't exist
        userValidator.validateUserDoesNotExist(userAuthDTO);
        return authService.signUp(
                userAuthDTO.getUsername(),
                userAuthDTO.getUsername(),
                userAuthDTO.getPassword()
        );
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signIn(@RequestBody LoginRequest loginRequest) {
        return authService.signIn(loginRequest);
    }

    /**
     * NEW ENDPOINT: Verify the JWT token from the HTTP-only cookie.
     * Returns 200 OK if valid, 401 if invalid or not present.
     */
    @GetMapping("/verify")
    public ResponseEntity<TokenVerificationResponse> verifyToken(HttpServletRequest request) {
        log.info("GET /api/auth/verify - Verifying token");

        // Ask the AuthService to check the token (it reads from the request cookie).
        boolean isValid = authService.verifyTokenFromRequest(request);

        if (isValid) {
            // Return a friendly message
            TokenVerificationResponse response = new TokenVerificationResponse(true, "Token is valid.");
            return ResponseEntity.ok(response);
        } else {
            // Return 401 if invalid
            TokenVerificationResponse response = new TokenVerificationResponse(false, "Invalid or missing token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Handles authentication-related endpoints:
 * – /signup  : new user registration
 * – /signin  : returns JWT + sets HttpOnly cookie
 * – /verify  : quick health-check for the token (used by Next.js middleware)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserValidator userValidator;

    /* ------------------------------------------------------------------
     *  Registration
     * ------------------------------------------------------------------ */
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserAuthDTO dto) {
        userValidator.validateUserDoesNotExist(dto);
        return authService.signUp(dto.getUsername(), dto.getUsername(), dto.getPassword());
    }

    /* ------------------------------------------------------------------
     *  Login – issue JWT in an HttpOnly cookie
     * ------------------------------------------------------------------ */
    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signIn(@RequestBody LoginRequest req) {

        // 1. Delegate to the service layer
        LoginResponse body = authService.signIn(req).getBody();           // contains the JWT

        // 2. Build a secure cookie (24 h TTL)
        assert body != null;
        ResponseCookie jwtCookie = ResponseCookie
                .from("jwtToken", body.getToken())
                .httpOnly(true)              // JS cannot read/overwrite
                .secure(true)                // sent only over HTTPS
                .sameSite("None")            // cross-site requests allowed
                .path("/")                   // available to the whole site
                .maxAge(Duration.ofDays(1))  // 24h
                .build();

        // 3. Return both the cookie and the JSON body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(body);
    }

    /* ------------------------------------------------------------------
     *  Lightweight token check (used by the frontend middleware)
     * ------------------------------------------------------------------ */
    @GetMapping("/verify")
    public ResponseEntity<TokenVerificationResponse> verifyToken(HttpServletRequest request) {
        log.info("GET /api/auth/verify – checking JWT from cookie");

        boolean valid = authService.verifyTokenFromRequest(request);
        TokenVerificationResponse resp =
                new TokenVerificationResponse(valid, valid ? "Token is valid" : "Invalid or missing token");

        return valid
                ? ResponseEntity.ok(resp)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }
}

package com.example.realestate.controller.auth;

import com.example.realestate.dto.auth.UserAuthDTO;
import com.example.realestate.dto.auth.LoginRequest;
import com.example.realestate.dto.auth.LoginResponse;
import com.example.realestate.service.auth.AuthService;
import com.example.realestate.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


}

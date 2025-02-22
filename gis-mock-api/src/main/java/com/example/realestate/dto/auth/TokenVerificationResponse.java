package com.example.realestate.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simple DTO to return whether the token is valid and a message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerificationResponse {
    private boolean valid;
    private String message;
}

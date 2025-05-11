package com.example.realestate.dto.google;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO for saving a user's Google OAuth2 token data.
 */
@Data
public class GoogleOAuthTokenDto {

    private String accessToken;
    private String refreshToken;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiresAt;

    private String scope;
}

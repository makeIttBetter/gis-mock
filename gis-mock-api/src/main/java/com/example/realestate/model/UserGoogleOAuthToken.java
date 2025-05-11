package com.example.realestate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Stores Google OAuth2 tokens (access token, refresh token, expiry, etc.) for a single User.
 */
@Entity
@Table(name = "user_google_oauth_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
public class UserGoogleOAuthToken extends Model {

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;  // Link to the 'users' table by ID

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // When does the access token expire?

    @Column(name = "scope")
    private String scope;

}

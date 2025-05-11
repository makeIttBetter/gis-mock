package com.example.realestate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Strong-typed holder for Google OAuth settings.
 * Values are injected from application-*.properties or the environment.
 */
@Configuration
@ConfigurationProperties(prefix = "google.oauth2")
@Getter
@Setter
public class GoogleOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scopes;
    private String authorizeBaseUrl;   // https://accounts.google.com/o/oauth2/v2/auth
    private String tokenBaseUrl;       // https://oauth2.googleapis.com/token
}

package com.example.realestate.service.google;

import com.example.realestate.config.GoogleOAuthProperties;
import com.example.realestate.model.UserGoogleOAuthToken;
import com.example.realestate.repository.UserGoogleOAuthTokenRepository;
import com.example.realestate.service.auth.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private final GoogleOAuthProperties props;
    private final UserGoogleOAuthTokenRepository tokenRepository;
    private final SecurityService securityService;
    private final RestTemplate restTemplate;

    /* ----------  PUBLIC API  ---------- */

    /**
     * Builds the “Click to sign in with Google” URL.
     * Always URL-encodes query params.
     */
    public String buildAuthorizationUrl() {
        return UriComponentsBuilder
                .fromUriString(props.getAuthorizeBaseUrl())
                .queryParam("client_id", props.getClientId())
                .queryParam("redirect_uri", urlEncode(props.getRedirectUri()))
                .queryParam("response_type", "code")
                // Ask for offline access to receive refresh_token the first time
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("scope", urlEncode(props.getScopes()))
                .build(false)               // don’t re-encode
                .toUriString();
    }

    /**
     * Exchanges Google’s one-time `code` for access + refresh tokens and stores them.
     */
    public void exchangeCodeForTokensAndSave(String code) {
        String userId = currentUser();            // throws if null

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("redirect_uri", props.getRedirectUri());
        form.add("grant_type", "authorization_code");

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

// ① use MultiValueMap  ② wrap in HttpEntity  ③ POST
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                props.getTokenBaseUrl(), entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Google token exchange failed: "
                    + response.getStatusCode());
        }

        Map<?, ?> json = response.getBody();
        saveOrUpdateTokens(
                (String) json.get("access_token"),
                (String) json.get("refresh_token"),
                ((Number) json.get("expires_in")).longValue(),   // seconds
                (String) json.get("scope"),
                userId
        );
    }

    /**
     * Returns <code>true</code> if the saved token exists *and* is recognised by Google.
     */
    public boolean verifyCurrentUserToken() {
        String userId = currentUser();
        Optional<UserGoogleOAuthToken> opt = tokenRepository.findByUserId(userId);
        if (opt.isEmpty()) return false;

        UserGoogleOAuthToken token = opt.get();
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.deleteByUserId(userId);
            return false;
        }
        return googleSaysTokenIsValid(token.getAccessToken());
    }

    /* ----------  INTERNAL HELPERS  ---------- */

    private void saveOrUpdateTokens(String access, String refresh,
                                    long expiresInSeconds, String scope, String userId) {

        UserGoogleOAuthToken entity = tokenRepository.findByUserId(userId)
                .orElseGet(() -> UserGoogleOAuthToken.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .build());

        entity.setAccessToken(access);
        entity.setRefreshToken(refresh);
        entity.setScope(scope);
        entity.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));

        tokenRepository.save(entity);
        log.info("Stored Google OAuth tokens for user {}", userId);
    }

    private boolean googleSaysTokenIsValid(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + accessToken;
        try {
            ResponseEntity<String> res = new RestTemplate().getForEntity(url, String.class);
            return res.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Google tokeninfo call failed: {}", ex.getMessage());
            return false;
        }
    }

    private String currentUser() {
        return Optional.ofNullable(securityService.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("No authenticated user."));
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}

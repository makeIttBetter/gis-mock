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

    public String buildAuthorizationUrl() {
        return UriComponentsBuilder
                .fromUriString(props.getAuthorizeBaseUrl())
                .queryParam("client_id", props.getClientId())
                .queryParam("redirect_uri", urlEncode(props.getRedirectUri()))
                .queryParam("response_type", "code")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("scope", urlEncode(props.getScopes()))
                .build(false)
                .toUriString();
    }

    public void exchangeCodeForTokensAndSave(String code) {
        String userId = currentUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("redirect_uri", props.getRedirectUri());
        form.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(props.getTokenBaseUrl(), entity, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Google token exchange failed: " + response.getStatusCode());
        }
        Map<?, ?> json = response.getBody();
        saveOrUpdateTokens(
                (String) json.get("access_token"),
                (String) json.get("refresh_token"),
                ((Number) json.get("expires_in")).longValue(),
                (String) json.get("scope"),
                userId
        );
    }

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

    public String getValidAccessToken(String userId) {
        UserGoogleOAuthToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No Google token found for user"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshAccessToken(token);
        }
        return token.getAccessToken();
    }

    public void refreshAccessToken(UserGoogleOAuthToken token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("refresh_token", token.getRefreshToken());
        form.add("grant_type", "refresh_token");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(props.getTokenBaseUrl(), entity, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to refresh Google token");
        }
        Map<?, ?> json = response.getBody();
        String newAccessToken = (String) json.get("access_token");
        long expiresInSeconds = ((Number) json.get("expires_in")).longValue();
        token.setAccessToken(newAccessToken);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));
        tokenRepository.save(token);
    }

    private void saveOrUpdateTokens(String access, String refresh, long expiresInSeconds, String scope, String userId) {
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
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
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
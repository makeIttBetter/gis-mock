package com.example.realestate.handler;

import com.example.realestate.dto.auth.UserAuthDTO;
import com.example.realestate.service.auth.OAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Called by Spring Security if OAuth2 login is successful.
 * We retrieve the user info from Google (for example),
 * then create/generate a JWT, and redirect the user to the front end with the token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2Service oauth2Service;

    @Value("${application.front-end.url:http://localhost:3000}")
    private String frontEndUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauthUser = oauthToken.getPrincipal();
            String username = oauthUser.getAttribute("name");   // Might differ based on provider
            String email = oauthUser.getAttribute("email");
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            log.info("OAuth2 login success. Provider: {}, User: {}, Email: {}", registrationId, username, email);

            // Build a small DTO
            UserAuthDTO userAuthDTO = new UserAuthDTO();
            userAuthDTO.setUsername(username != null ? username : email);
            userAuthDTO.setPassword(""); // no local password
            userAuthDTO.setUsername(email);
            userAuthDTO.setProvider(registrationId);

            // Let our OAuth2Service do the rest (create or update user, produce JWT)
            String jwtToken = oauth2Service.handleOAuthLogin(userAuthDTO);

            // Finally, redirect to front-end with token
            response.sendRedirect(frontEndUrl + "/auth-redirect?token=" + jwtToken);
        } else {
            throw new IllegalArgumentException("Authentication is not an OAuth2AuthenticationToken");
        }
    }
}

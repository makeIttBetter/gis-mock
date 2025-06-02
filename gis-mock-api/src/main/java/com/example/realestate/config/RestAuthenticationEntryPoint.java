package com.example.realestate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Sends JSON 401 responses **without** the `WWW-Authenticate`
 * header that makes browsers show the Basic-Auth dialog.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        /* ← THIS is the line that prevents the native login popup */
        response.setHeader("WWW-Authenticate", "");

        mapper.writeValue(
                response.getOutputStream(),
                Map.of("message", "Unauthorized")
        );
    }
}

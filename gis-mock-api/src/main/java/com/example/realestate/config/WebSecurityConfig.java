package com.example.realestate.config;

import com.example.realestate.filter.JwtRequestFilter;
import com.example.realestate.handler.CustomAuthenticationFailureHandler;
import com.example.realestate.handler.CustomAuthenticationSuccessHandler;
import com.example.realestate.service.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Web Security configuration.
 * Configures HTTP security, OAuth2 login, JWT filter, etc.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    /**
     * Tells Spring how to build the AuthenticationManager
     * with our own UserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder); // Use the injected PasswordEncoder

        return builder.build();
    }

    /**
     * SecurityFilterChain to configure the actual rules for HTTP endpoints,
     * add our JWT filter, and set up OAuth2 login handlers.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for simplicity (use as needed)
                .csrf(AbstractHttpConfigurer::disable)
//                .cors(Customizer.withDefaults())
                // Our endpoint authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Permit these endpoints without JWT
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/signin",
                                "/api/auth/verify",
                                // openApi endpoints
                                "/api/openApi/**",
                                "/api/gis/**",
                                // Add any public endpoints, like your map endpoints
                                "/api/google/oauth/callback",  // If needed for Google callback
                                "/api/google/oauth/init",      // Start OAuth2 flow
                                // etc. ...
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Require authentication for everything else
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                // If you want to customize headers
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authEndpoint -> authEndpoint.baseUri("/oauth2/authorization"))
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                )

                // Add our JWT filter before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

                // Example: If you had a "UserLastActiveUpdateFilter" you can chain it like:
                //.addFilterAfter(userLastActiveUpdateFilter, JwtRequestFilter.class)

                // Build the config
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

}

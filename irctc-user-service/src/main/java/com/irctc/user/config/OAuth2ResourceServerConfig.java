package com.irctc.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * OAuth2 Resource Server Configuration
 * 
 * This configuration enables OAuth2/OIDC support for the User Service.
 * It supports both JWT tokens from an OAuth2 authorization server
 * and maintains backward compatibility with existing JWT tokens.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.jwt.issuer-uri")
public class OAuth2ResourceServerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:#{null}}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:#{null}}")
    private String issuerUri;

    /**
     * Security filter chain that configures OAuth2 resource server
     * with JWT token validation.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(
                    "/actuator/**",
                    "/api/v1/users/register",
                    "/api/v1/users/login",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/v3/api-docs/**",
                    "/error"
                ).permitAll()
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**", "/.well-known/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> {
                JwtDecoder decoder = jwtDecoder();
                if (decoder != null) {
                    oauth2.jwt(jwt -> jwt
                        .decoder(decoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    );
                }
            });

        return http.build();
    }

    /**
     * JWT Decoder that supports both JWK Set URI and Issuer URI.
     * Uses issuer URI to auto-discover JWK Set URI.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        if (jwkSetUri != null && !jwkSetUri.isEmpty()) {
            // Use JWK Set URI if provided
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else if (issuerUri != null && !issuerUri.isEmpty()) {
            // Use Issuer URI to discover JWK Set URI
            return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        } else {
            // If OAuth2 is not configured, return null (will be handled by conditional configuration)
            // This allows the application to work with or without OAuth2
            return null;
        }
    }

    /**
     * JWT Authentication Converter that extracts authorities from JWT claims.
     * Supports both 'authorities' and 'scope' claims.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        // Extract authorities from 'authorities' claim
        authoritiesConverter.setAuthoritiesClaimName("authorities");
        // Also check 'scope' claim as fallback
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = 
            new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return authenticationConverter;
    }
}


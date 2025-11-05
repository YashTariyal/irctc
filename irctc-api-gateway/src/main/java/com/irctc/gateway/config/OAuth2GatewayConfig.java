package com.irctc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * OAuth2 Configuration for API Gateway
 * 
 * Configures JWT decoder for validating OAuth2 tokens at the gateway level.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
public class OAuth2GatewayConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    /**
     * JWT Decoder that validates tokens from the OAuth2 authorization server.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        if (issuerUri == null || issuerUri.isEmpty()) {
            throw new IllegalStateException(
                "OAuth2 issuer URI not configured. " +
                "Please set spring.security.oauth2.resourceserver.jwt.issuer-uri"
            );
        }
        
        // Use issuer URI to auto-discover JWK Set URI
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }
}


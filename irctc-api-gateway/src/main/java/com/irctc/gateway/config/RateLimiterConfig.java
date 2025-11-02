package com.irctc.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate Limiter Configuration
 * 
 * Provides key resolvers for rate limiting:
 * - IP-based rate limiting (default)
 * - User-based rate limiting (when authenticated)
 * - API key-based rate limiting
 * 
 * @author IRCTC Development Team
 * @version 2.0.0
 */
@Configuration
public class RateLimiterConfig {

    /**
     * IP-based key resolver (default)
     * Uses X-Forwarded-For header or remote address
     */
    @Bean(name = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")
        ).switchIfEmpty(
                Mono.just(exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown")
        );
    }

    /**
     * User-based key resolver
     * Uses JWT token subject (user ID) or username from authentication
     * Falls back to IP if user not authenticated
     */
    @Bean(name = "userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get user ID from JWT token
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just(userId);
            }
            
            // Try to get from Authorization header (JWT)
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // In a real implementation, decode JWT and extract user ID
                // For now, use a hash of the token as identifier
                String token = authHeader.substring(7);
                return Mono.just("user-" + String.valueOf(token.hashCode()));
            }
            
            // Fallback to IP-based
            return ipKeyResolver().resolve(exchange);
        };
    }

    /**
     * API key-based key resolver
     * Uses X-API-Key header
     * Falls back to user, then IP if no API key
     */
    @Bean(name = "apiKeyResolver")
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return Mono.just("api-" + apiKey);
            }
            
            // Fallback to user-based, then IP-based
            return userKeyResolver().resolve(exchange);
        };
    }
}

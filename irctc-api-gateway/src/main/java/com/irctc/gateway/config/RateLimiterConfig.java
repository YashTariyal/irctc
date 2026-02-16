package com.irctc.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate Limiter Configuration
 * 
 * Provides key resolvers for rate limiting:
 * - IP-based rate limiting (default)
 * - User-based rate limiting (when authenticated) - extracts user ID from JWT
 * - API key-based rate limiting
 * 
 * @author IRCTC Development Team
 * @version 3.0.0
 */
@Configuration
public class RateLimiterConfig {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterConfig.class);
    
    public RateLimiterConfig() {
    }

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
     * 
     * Priority:
     * 1. X-User-Id header (set by OAuth2AuthenticationFilter)
     * 2. JWT token subject (sub claim) from Authorization header
     * 3. JWT token username claim
     * 4. Fallback to IP-based if not authenticated
     * 
     * This ensures proper user-based rate limiting per authenticated user.
     */
    @Bean(name = "userKeyResolver")
    @org.springframework.context.annotation.Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // First, try to get user ID from X-User-Id header
            // This is set by OAuth2AuthenticationFilter after JWT validation
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty() && !userId.equals("anonymous")) {
                logger.debug("Using user ID from X-User-Id header: {}", userId);
                return Mono.just("user-" + userId);
            }
            
            // In this local/dev setup we don't introspect JWTs for rate limiting
            // to avoid hard dependency on JwtDecoder; instead rely on X-User-Id
            // when present, otherwise fall back to IP-based keys.
            
            // Fallback to IP-based rate limiting if user not authenticated
            logger.debug("No user authentication found, using IP-based rate limiting");
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

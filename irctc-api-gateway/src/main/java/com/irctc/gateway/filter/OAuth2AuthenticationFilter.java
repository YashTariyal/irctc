package com.irctc.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * OAuth2 Authentication Filter for API Gateway
 * 
 * This filter validates OAuth2 JWT tokens at the gateway level
 * and forwards user information to downstream services.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class OAuth2AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationFilter.class);
    
    private final JwtDecoder jwtDecoder;
    
    public OAuth2AuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for public endpoints
        String path = request.getURI().getPath();
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }
        
        // Extract token from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Authorization header found for path: {}", path);
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // Decode and validate JWT token
            Jwt jwt = jwtDecoder.decode(token);
            
            // Extract user information from token
            String username = jwt.getClaimAsString("sub");
            String email = jwt.getClaimAsString("email");
            List<String> authorities = jwt.getClaimAsStringList("authorities");
            
            logger.debug("Authenticated user: {} with authorities: {}", username, authorities);
            
            // Add user information to request headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", username != null ? username : "")
                .header("X-User-Email", email != null ? email : "")
                .header("X-User-Authorities", authorities != null ? String.join(",", authorities) : "")
                .header("X-Authenticated", "true")
                .build();
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (JwtException e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            return unauthorized(exchange, "Invalid or expired token: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing OAuth2 token", e);
            return unauthorized(exchange, "Authentication error");
        }
    }

    /**
     * Check if the endpoint is public (doesn't require authentication).
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/oauth2") ||
               path.startsWith("/.well-known") ||
               path.equals("/health") ||
               path.equals("/");
    }

    /**
     * Return 401 Unauthorized response.
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\":\"unauthorized\",\"message\":\"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        // Run before other filters
        return -100;
    }
}


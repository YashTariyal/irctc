package com.irctc.gateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Correlation ID Gateway Filter
 * 
 * Adds correlation ID to all requests passing through the API Gateway.
 * Propagates correlation ID from incoming requests or generates a new one.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final int FILTER_ORDER = -100; // Execute early
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        
        // Generate new correlation ID if not present
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Add to MDC for logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        // Add correlation ID to request headers for downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
            .header(CORRELATION_ID_HEADER, correlationId)
            .build();
        
        // Add correlation ID to response headers
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        
        // Continue with modified request
        ServerWebExchange modifiedExchange = exchange.mutate()
            .request(modifiedRequest)
            .build();
        
        return chain.filter(modifiedExchange)
            .doFinally(signalType -> {
                // Clean up MDC
                MDC.remove(CORRELATION_ID_MDC_KEY);
            });
    }
    
    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}


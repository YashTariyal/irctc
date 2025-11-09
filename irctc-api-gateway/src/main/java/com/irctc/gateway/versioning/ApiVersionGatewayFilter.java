package com.irctc.gateway.versioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * API Version Gateway Filter
 * 
 * Handles version negotiation and adds version headers to responses
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class ApiVersionGatewayFilter extends AbstractGatewayFilterFactory<ApiVersionGatewayFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionGatewayFilter.class);
    
    @Autowired
    private ApiVersionResolver versionResolver;
    
    @Autowired
    private ApiVersionManager versionManager;
    
    public ApiVersionGatewayFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            
            // Resolve API version
            String version = versionResolver.resolveVersion(request);
            if (version == null) {
                version = versionResolver.getDefaultVersion();
            }
            
            // Check if version is supported
            if (!versionResolver.isVersionSupported(version)) {
                logger.warn("Unsupported API version requested: {}", version);
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                response.getHeaders().add("X-Error", "Unsupported API version: " + version);
                return response.setComplete();
            }
            
            // Check if version is deprecated
            ApiVersionInfo versionInfo = versionManager.getVersionInfo(version);
            if (versionInfo != null && versionInfo.isDeprecated()) {
                addDeprecationWarning(response, versionInfo);
            }
            
            // Add version headers to response
            response.getHeaders().add("X-API-Version", version);
            response.getHeaders().add("X-API-Version-Status", 
                versionInfo != null && versionInfo.isDeprecated() ? "deprecated" : "active");
            
            // Add version to request attributes for downstream services
            exchange.getAttributes().put("api.version", version);
            
            logger.debug("API version resolved: {} for path: {}", version, request.getURI().getPath());
            
            return chain.filter(exchange);
        };
    }
    
    private void addDeprecationWarning(ServerHttpResponse response, ApiVersionInfo versionInfo) {
        StringBuilder warning = new StringBuilder("299 - \"API version ");
        warning.append(versionInfo.getVersion());
        warning.append(" is deprecated");
        
        if (versionInfo.getSunsetDate() != null) {
            warning.append(". Sunset date: ");
            warning.append(versionInfo.getSunsetDate().format(DateTimeFormatter.ISO_DATE));
        }
        
        if (versionInfo.getReplacementVersion() != null) {
            warning.append(". Please migrate to ");
            warning.append(versionInfo.getReplacementVersion());
        }
        
        warning.append("\"");
        
        response.getHeaders().add("Warning", warning.toString());
        response.getHeaders().add("X-API-Deprecated", "true");
        
        if (versionInfo.getSunsetDate() != null) {
            response.getHeaders().add("X-API-Sunset", 
                versionInfo.getSunsetDate().format(DateTimeFormatter.ISO_DATE));
        }
        
        if (versionInfo.getReplacementVersion() != null) {
            response.getHeaders().add("X-API-Replacement", versionInfo.getReplacementVersion());
        }
    }
    
    public static class Config {
        // Configuration properties if needed
    }
}


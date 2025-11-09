package com.irctc.booking.tenant;

import com.irctc.booking.tenant.entity.Tenant;
import com.irctc.booking.tenant.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Tenant Resolver
 * 
 * Intercepts HTTP requests to extract and set tenant context
 * Supports multiple tenant identification methods:
 * 1. X-Tenant-Id header
 * 2. X-Tenant-Code header
 * 3. Subdomain (e.g., tenant1.example.com)
 * 4. JWT claim (if integrated with OAuth2)
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class TenantResolver implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantResolver.class);
    
    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String TENANT_CODE_HEADER = "X-Tenant-Code";
    
    @Autowired(required = false)
    private TenantRepository tenantRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           jakarta.servlet.http.HttpServletResponse response, 
                           Object handler) throws Exception {
        
        // Skip tenant resolution for actuator endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || 
            path.startsWith("/swagger") || 
            path.startsWith("/api-docs") ||
            path.startsWith("/api/tenants") && request.getMethod().equals("POST")) {
            return true;
        }
        
        // 1. Try to get tenant from header (X-Tenant-Id)
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        if (StringUtils.hasText(tenantId)) {
            TenantContext.setTenantId(tenantId);
            logger.debug("Tenant ID resolved from header: {}", tenantId);
            return true;
        }
        
        // 2. Try to get tenant from header (X-Tenant-Code)
        String tenantCode = request.getHeader(TENANT_CODE_HEADER);
        if (StringUtils.hasText(tenantCode)) {
            // Resolve tenant code to tenant ID
            if (tenantRepository != null) {
                Optional<Tenant> tenant = tenantRepository.findActiveByCode(tenantCode);
                if (tenant.isPresent()) {
                    TenantContext.setTenantId(tenant.get().getId().toString());
                    TenantContext.setTenantCode(tenantCode);
                    logger.debug("Tenant resolved from code: {} -> {}", tenantCode, tenant.get().getId());
                    return true;
                } else {
                    logger.warn("Tenant not found or inactive: {}", tenantCode);
                    response.setStatus(403);
                    response.getWriter().write("{\"error\":\"Invalid or inactive tenant\"}");
                    return false;
                }
            } else {
                // If repository not available, just set the code
                TenantContext.setTenantCode(tenantCode);
                logger.debug("Tenant code set (repository not available): {}", tenantCode);
                return true;
            }
        }
        
        // 3. Try to extract from subdomain
        String host = request.getHeader("Host");
        if (StringUtils.hasText(host)) {
            String tenantFromSubdomain = extractTenantFromSubdomain(host);
            if (StringUtils.hasText(tenantFromSubdomain)) {
                if (tenantRepository != null) {
                    Optional<Tenant> tenant = tenantRepository.findActiveByCode(tenantFromSubdomain);
                    if (tenant.isPresent()) {
                        TenantContext.setTenantId(tenant.get().getId().toString());
                        TenantContext.setTenantCode(tenantFromSubdomain);
                        logger.debug("Tenant resolved from subdomain: {} -> {}", tenantFromSubdomain, tenant.get().getId());
                        return true;
                    }
                } else {
                    TenantContext.setTenantCode(tenantFromSubdomain);
                    logger.debug("Tenant code set from subdomain: {}", tenantFromSubdomain);
                    return true;
                }
            }
        }
        
        // 4. TODO: Extract from JWT claim (if OAuth2 is integrated)
        // This would require integration with Spring Security
        
        // No tenant found - return error for protected endpoints
        logger.warn("No tenant context found for request: {}", path);
        response.setStatus(400);
        response.getWriter().write("{\"error\":\"Tenant context is required. Please provide X-Tenant-Id or X-Tenant-Code header\"}");
        return false;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               jakarta.servlet.http.HttpServletResponse response, 
                               Object handler, 
                               Exception ex) throws Exception {
        // Clear tenant context after request completion
        TenantContext.clear();
    }
    
    /**
     * Extract tenant code from subdomain
     * Example: tenant1.example.com -> tenant1
     */
    private String extractTenantFromSubdomain(String host) {
        if (host == null || host.isEmpty()) {
            return null;
        }
        
        // Remove port if present
        String hostWithoutPort = host.split(":")[0];
        
        // Split by dot
        String[] parts = hostWithoutPort.split("\\.");
        
        // If we have at least 3 parts (tenant.domain.com), return first part
        if (parts.length >= 3) {
            return parts[0];
        }
        
        // For localhost or IP addresses, return null
        return null;
    }
}


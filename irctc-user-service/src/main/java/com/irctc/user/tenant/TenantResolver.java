package com.irctc.user.tenant;

import com.irctc.user.tenant.entity.Tenant;
import com.irctc.user.tenant.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

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
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || 
            path.startsWith("/swagger") || 
            path.startsWith("/api-docs") ||
            path.startsWith("/api/tenants") && request.getMethod().equals("POST")) {
            return true;
        }
        
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        if (StringUtils.hasText(tenantId)) {
            TenantContext.setTenantId(tenantId);
            logger.debug("Tenant ID resolved from header: {}", tenantId);
            return true;
        }
        
        String tenantCode = request.getHeader(TENANT_CODE_HEADER);
        if (StringUtils.hasText(tenantCode)) {
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
                TenantContext.setTenantCode(tenantCode);
                logger.debug("Tenant code set (repository not available): {}", tenantCode);
                return true;
            }
        }
        
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
        TenantContext.clear();
    }
    
    private String extractTenantFromSubdomain(String host) {
        if (host == null || host.isEmpty()) {
            return null;
        }
        String hostWithoutPort = host.split(":")[0];
        String[] parts = hostWithoutPort.split("\\.");
        if (parts.length >= 3) {
            return parts[0];
        }
        return null;
    }
}


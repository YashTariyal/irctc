package com.irctc.booking.tenant.config;

import com.irctc.booking.tenant.TenantResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Tenant Configuration
 * 
 * Registers tenant resolver interceptor
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
public class TenantConfig implements WebMvcConfigurer {
    
    @Autowired
    private TenantResolver tenantResolver;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantResolver)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/api/tenants/**" // Allow tenant management endpoints
                );
    }
}


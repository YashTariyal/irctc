package com.irctc.booking.config;

import com.irctc.booking.versioning.ApiVersionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * 
 * Configures interceptors and other web-related settings
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private ApiVersionInterceptor apiVersionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (apiVersionInterceptor != null) {
            registry.addInterceptor(apiVersionInterceptor)
                    .addPathPatterns("/api/**");
        }
    }
}


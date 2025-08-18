package com.irctc_backend.irctc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class for AOP (Aspect-Oriented Programming).
 * This class enables AOP functionality and configures aspect execution.
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AopConfig {
    
    // Additional AOP configurations can be added here if needed
    // For example, configuring aspect precedence, etc.
}

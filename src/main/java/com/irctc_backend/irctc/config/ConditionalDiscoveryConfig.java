package com.irctc_backend.irctc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional configuration for service discovery
 * Only enables discovery client when explicitly configured
 */
@Configuration
@ConditionalOnProperty(
    name = "eureka.client.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
@EnableDiscoveryClient
public class ConditionalDiscoveryConfig {
    // This configuration will only be active when eureka.client.enabled=true
}

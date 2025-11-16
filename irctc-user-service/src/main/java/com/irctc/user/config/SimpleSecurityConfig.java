package com.irctc.user.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Simple Security Configuration (Fallback)
 * 
 * This is a simplified security configuration used when OAuth2 is not enabled.
 * It can be enabled by setting: security.simple.enabled=true
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.simple.enabled", havingValue = "true", matchIfMissing = false)
public class SimpleSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Explicitly allow social login endpoints
                .requestMatchers(
                    "/api/auth/google",
                    "/api/auth/facebook",
                    "/api/auth/apple",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/api-docs/**"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());
        
        return http.build();
    }
}

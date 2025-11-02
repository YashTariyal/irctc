package com.irctc_backend.irctc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * 
 * Configures API documentation for the IRCTC application.
 * Access Swagger UI at: /swagger-ui.html
 * Access API docs at: /v3/api-docs
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IRCTC Railway Booking System API")
                        .version("1.0.0")
                        .description("RESTful API for Indian Railway Ticket Booking System. " +
                                   "Provides endpoints for train search, booking, payment, and user management.")
                        .contact(new Contact()
                                .name("IRCTC Development Team")
                                .email("support@irctc.com")
                                .url("https://github.com/irctc"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.irctc.com")
                                .description("Production Server"),
                        new Server()
                                .url("https://api-staging.irctc.com")
                                .description("Staging Server")
                ));
    }
}

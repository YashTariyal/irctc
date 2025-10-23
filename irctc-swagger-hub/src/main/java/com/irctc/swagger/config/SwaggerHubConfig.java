package com.irctc.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger Hub Configuration for IRCTC Microservices
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
public class SwaggerHubConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IRCTC Microservices API Hub")
                        .description("Central API Documentation for all IRCTC Microservices")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IRCTC Development Team")
                                .email("dev@irctc.com")
                                .url("https://irctc.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8096").description("Swagger Hub Direct"),
                        new Server().url("http://localhost:8090").description("API Gateway"),
                        new Server().url("http://localhost:8761").description("Eureka Server")
                ));
    }
}

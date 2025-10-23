package com.irctc.train.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger Configuration for IRCTC Train Service
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
                        .title("IRCTC Train Service API")
                        .description("Train Information, Search, and Management Service for IRCTC Microservices")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IRCTC Development Team")
                                .email("dev@irctc.com")
                                .url("https://irctc.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8092").description("Train Service Direct"),
                        new Server().url("http://localhost:8090/api/trains").description("Train Service via API Gateway")
                ));
    }
}

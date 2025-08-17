package com.irctc_backend.irctc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI irctcOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development server");
        
        Server prodServer = new Server();
        prodServer.setUrl("https://irctc-backend.com");
        prodServer.setDescription("Production server");
        
        Contact contact = new Contact();
        contact.setEmail("support@irctc.com");
        contact.setName("IRCTC Support Team");
        contact.setUrl("https://www.irctc.co.in");
        
        License mitLicense = new License()
            .name("MIT License")
            .url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
            .title("IRCTC Backend API")
            .version("1.0.0")
            .contact(contact)
            .description("This API provides endpoints for IRCTC (Indian Railway Catering and Tourism Corporation) backend services including user management, train booking, and notification systems.")
            .termsOfService("https://www.irctc.co.in/terms")
            .license(mitLicense);
        
        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer, prodServer));
    }
} 
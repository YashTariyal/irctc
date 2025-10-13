package com.irctc_backend.irctc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.examples.Example;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI irctcOpenAPI() {
        // Server configurations
        Server devServer = new Server()
            .url("http://localhost:8082")
            .description("🚀 Development Server - Use this for testing and development");
        
        Server prodServer = new Server()
            .url("https://api.irctc.com")
            .description("🌐 Production Server - Live IRCTC API");
        
        // Contact information
        Contact contact = new Contact()
            .email("api-support@irctc.com")
            .name("IRCTC API Support Team")
            .url("https://www.irctc.co.in/support");
        
        // License information
        License license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");
        
        // API Information
        Info info = new Info()
            .title("🚂 IRCTC Backend API")
            .version("1.0.0")
            .contact(contact)
            .description("""
                # IRCTC Backend API Documentation
                
                Welcome to the **Indian Railway Catering and Tourism Corporation (IRCTC)** Backend API! 
                This comprehensive API provides all the necessary endpoints for building railway booking applications.
                
                ## 🎯 Key Features
                - **User Management**: Registration, authentication, and profile management
                - **Train Operations**: Train schedules, routes, and availability
                - **Booking System**: Complete booking lifecycle with real-time updates
                - **Passenger Management**: Passenger details and preferences
                - **Event-Driven Architecture**: Real-time notifications via Kafka
                - **Comprehensive Monitoring**: Metrics, tracing, and health checks
                
                ## 🔐 Authentication
                This API uses JWT-based authentication. Include the token in the Authorization header:
                ```
                Authorization: Bearer <your-jwt-token>
                ```
                
                ## 📊 Rate Limiting
                - **Free Tier**: 100 requests/hour
                - **Premium**: 1000 requests/hour
                - **Enterprise**: Custom limits
                
                ## 🚀 Getting Started
                1. Register for an API key
                2. Explore the endpoints below
                3. Use the "Try it out" feature to test APIs
                4. Check the examples for request/response formats
                
                ## 📞 Support
                - **Documentation**: [API Docs](https://docs.irctc.com)
                - **Support**: api-support@irctc.com
                - **Status**: [API Status](https://status.irctc.com)
                """)
            .termsOfService("https://www.irctc.co.in/terms")
            .license(license);
        
        // API Tags for better organization
        List<Tag> tags = List.of(
            new Tag().name("👥 Users").description("User registration, authentication, and profile management"),
            new Tag().name("🚂 Trains").description("Train information, schedules, and route management"),
            new Tag().name("🎫 Bookings").description("Booking creation, modification, and cancellation"),
            new Tag().name("👤 Passengers").description("Passenger information and preferences"),
            new Tag().name("📊 Dashboard").description("Analytics, statistics, and monitoring"),
            new Tag().name("🔧 System").description("Health checks, metrics, and system information")
        );
        
        // Security scheme
        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT token obtained from authentication endpoint");
        
        // Global security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList("bearerAuth");
        
        // Components for reusable schemas and examples
        Components components = new Components()
            .addSecuritySchemes("bearerAuth", securityScheme)
            .addExamples("successResponse", createSuccessExample())
            .addExamples("errorResponse", createErrorExample())
            .addExamples("validationError", createValidationErrorExample());
        
        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer, prodServer))
            .tags(tags)
            .components(components)
            .addSecurityItem(securityRequirement);
    }
    
    /**
     * Configure Swagger to only include backend API controllers
     * Excludes frontend controllers (Dashboard, Test) from Swagger documentation
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("🚀 IRCTC Backend API")
                .packagesToScan("com.irctc_backend.irctc.controller")
                .pathsToMatch("/api/**")
                .displayName("IRCTC Backend API")
                .build();
    }
    
    /**
     * User Management API Group
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("👥 User Management")
                .pathsToMatch("/api/users/**")
                .displayName("User Management APIs")
                .build();
    }
    
    /**
     * Train Management API Group
     */
    @Bean
    public GroupedOpenApi trainApi() {
        return GroupedOpenApi.builder()
                .group("🚂 Train Management")
                .pathsToMatch("/api/trains/**")
                .displayName("Train Management APIs")
                .build();
    }
    
    /**
     * Booking Management API Group
     */
    @Bean
    public GroupedOpenApi bookingApi() {
        return GroupedOpenApi.builder()
                .group("🎫 Booking Management")
                .pathsToMatch("/api/bookings/**")
                .displayName("Booking Management APIs")
                .build();
    }
    
    /**
     * Passenger Management API Group
     */
    @Bean
    public GroupedOpenApi passengerApi() {
        return GroupedOpenApi.builder()
                .group("👤 Passenger Management")
                .pathsToMatch("/api/passengers/**")
                .displayName("Passenger Management APIs")
                .build();
    }
    
    /**
     * System & Monitoring API Group
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("🔧 System & Monitoring")
                .pathsToMatch("/actuator/**", "/api/dashboard/**")
                .displayName("System & Monitoring APIs")
                .build();
    }
    
    // Helper methods for creating examples
    private Example createSuccessExample() {
        Example example = new Example();
        example.setSummary("Success Response");
        example.setDescription("Standard success response format");
        example.setValue(Map.of(
            "success", true,
            "message", "Operation completed successfully",
            "data", Map.of(
                "id", 1,
                "name", "Example Data",
                "createdAt", "2025-10-13T22:30:00Z"
            ),
            "timestamp", "2025-10-13T22:30:00Z"
        ));
        return example;
    }
    
    private Example createErrorExample() {
        Example example = new Example();
        example.setSummary("Error Response");
        example.setDescription("Standard error response format");
        example.setValue(Map.of(
            "success", false,
            "error", Map.of(
                "code", "INTERNAL_SERVER_ERROR",
                "message", "An unexpected error occurred",
                "details", "Please contact support if the problem persists"
            ),
            "timestamp", "2025-10-13T22:30:00Z",
            "path", "/api/example"
        ));
        return example;
    }
    
    private Example createValidationErrorExample() {
        Example example = new Example();
        example.setSummary("Validation Error Response");
        example.setDescription("Validation error response format");
        example.setValue(Map.of(
            "success", false,
            "error", Map.of(
                "code", "VALIDATION_ERROR",
                "message", "Request validation failed",
                "details", List.of(
                    Map.of("field", "email", "message", "Email is required"),
                    Map.of("field", "password", "message", "Password must be at least 8 characters")
                )
            ),
            "timestamp", "2025-10-13T22:30:00Z",
            "path", "/api/users/register"
        ));
        return example;
    }
} 
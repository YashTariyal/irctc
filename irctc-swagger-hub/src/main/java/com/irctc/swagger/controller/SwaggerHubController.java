package com.irctc.swagger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Swagger Hub Controller for IRCTC Microservices
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
public class SwaggerHubController {

    @GetMapping("/")
    public ModelAndView redirectToSwagger() {
        return new ModelAndView("redirect:/swagger-ui/index.html");
    }

    @GetMapping("/services")
    public String getAvailableServices() {
        return """
                {
                    "services": [
                        {
                            "name": "User Service",
                            "url": "http://localhost:8091/swagger-ui.html",
                            "description": "User Management and Authentication"
                        },
                        {
                            "name": "Train Service", 
                            "url": "http://localhost:8092/swagger-ui.html",
                            "description": "Train Information and Search"
                        },
                        {
                            "name": "Booking Service",
                            "url": "http://localhost:8093/swagger-ui.html", 
                            "description": "Ticket Booking and Management"
                        },
                        {
                            "name": "Payment Service",
                            "url": "http://localhost:8094/swagger-ui.html",
                            "description": "Payment Processing"
                        },
                        {
                            "name": "Notification Service",
                            "url": "http://localhost:8095/swagger-ui.html",
                            "description": "Notifications and Alerts"
                        }
                    ],
                    "eureka": "http://localhost:8761",
                    "apiGateway": "http://localhost:8090"
                }
                """;
    }
}

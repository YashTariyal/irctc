package com.irctc.eureka.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Swagger Dashboard Controller for Eureka Server
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
public class SwaggerDashboardController {

    @GetMapping("/swagger-dashboard")
    public ModelAndView swaggerDashboard() {
        return new ModelAndView("swagger-dashboard");
    }

    @GetMapping("/swagger")
    public ModelAndView redirectToSwaggerDashboard() {
        return new ModelAndView("redirect:/swagger-dashboard");
    }
}

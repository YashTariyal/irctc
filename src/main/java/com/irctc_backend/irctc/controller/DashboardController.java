package com.irctc_backend.irctc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for serving the AOP timing dashboard view.
 * Provides the dashboard HTML page.
 */
@Controller
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "AOP Timing Dashboard view")
@Hidden
public class DashboardController {
    
    /**
     * Serve the main dashboard HTML page.
     */
    @GetMapping
    @Operation(summary = "Get Dashboard", description = "Returns the main dashboard HTML page")
    public String getDashboard() {
        return "dashboard"; // This will resolve to dashboard.html in templates folder
    }
}

package com.irctc.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * Feign client for Train Service integration
 */
@FeignClient(name = "irctc-train-service", fallback = TrainServiceClientFallback.class)
public interface TrainServiceClient {
    
    /**
     * Get all routes
     */
    @GetMapping("/api/trains/routes")
    List<RouteDTO> getAllRoutes();
    
    /**
     * Get route by ID
     */
    @GetMapping("/api/trains/routes/{routeId}")
    RouteDTO getRouteById(@PathVariable Long routeId);
    
    /**
     * Get popular routes
     */
    @GetMapping("/api/trains/routes/popular")
    List<RouteDTO> getPopularRoutes();
    
    /**
     * Route DTO
     */
    class RouteDTO {
        private Long id;
        private String sourceStation;
        private String destinationStation;
        private String routeCode;
        private Double distance;
        private Integer popularity;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getSourceStation() { return sourceStation; }
        public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }
        public String getDestinationStation() { return destinationStation; }
        public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }
        public String getRouteCode() { return routeCode; }
        public void setRouteCode(String routeCode) { this.routeCode = routeCode; }
        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }
        public Integer getPopularity() { return popularity; }
        public void setPopularity(Integer popularity) { this.popularity = popularity; }
    }
}


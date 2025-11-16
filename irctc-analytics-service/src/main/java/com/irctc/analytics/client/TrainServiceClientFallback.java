package com.irctc.analytics.client;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback implementation for Train Service Client
 */
@Component
public class TrainServiceClientFallback implements TrainServiceClient {
    
    @Override
    public List<RouteDTO> getAllRoutes() {
        return new ArrayList<>();
    }
    
    @Override
    public RouteDTO getRouteById(Long routeId) {
        return new RouteDTO();
    }
    
    @Override
    public List<RouteDTO> getPopularRoutes() {
        return new ArrayList<>();
    }
}


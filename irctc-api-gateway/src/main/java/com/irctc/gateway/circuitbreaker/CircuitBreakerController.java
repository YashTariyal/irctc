package com.irctc.gateway.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Circuit Breaker Management Controller
 * 
 * Provides REST API for monitoring and managing circuit breakers
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/circuit-breakers")
public class CircuitBreakerController {

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Autowired(required = false)
    private CircuitBreakerStateChangeListener stateChangeListener;
    
    /**
     * Get all circuit breakers status
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCircuitBreakers() {
        Map<String, Object> response = new HashMap<>();
        
        if (circuitBreakerRegistry == null) {
            response.put("error", "Circuit breaker registry not available");
            return ResponseEntity.ok(response);
        }
        
        Map<String, Map<String, Object>> circuitBreakers = circuitBreakerRegistry
            .getAllCircuitBreakers()
            .stream()
            .collect(Collectors.toMap(
                CircuitBreaker::getName,
                this::getCircuitBreakerInfo
            ));
        
        response.put("circuitBreakers", circuitBreakers);
        response.put("totalCount", circuitBreakers.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get specific circuit breaker status
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getCircuitBreaker(@PathVariable String name) {
        if (circuitBreakerRegistry == null) {
            return ResponseEntity.notFound().build();
        }
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.find(name).orElse(null);
        if (circuitBreaker == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(getCircuitBreakerInfo(circuitBreaker));
    }
    
    /**
     * Get circuit breaker metrics
     */
    @GetMapping("/{name}/metrics")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerMetrics(@PathVariable String name) {
        if (circuitBreakerRegistry == null) {
            return ResponseEntity.notFound().build();
        }
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.find(name).orElse(null);
        if (circuitBreaker == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("name", circuitBreaker.getName());
        metrics.put("state", circuitBreaker.getState().name());
        metrics.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
        metrics.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
        metrics.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        metrics.put("numberOfNotPermittedCalls", circuitBreaker.getMetrics().getNumberOfNotPermittedCalls());
        metrics.put("numberOfBufferedCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls());
        metrics.put("numberOfSlowCalls", circuitBreaker.getMetrics().getNumberOfSlowCalls());
        metrics.put("slowCallRate", circuitBreaker.getMetrics().getSlowCallRate());
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Transition circuit breaker to a specific state (for testing/admin)
     */
    @PostMapping("/{name}/transition")
    public ResponseEntity<Map<String, Object>> transitionState(
            @PathVariable String name,
            @RequestParam String state) {
        
        if (circuitBreakerRegistry == null) {
            return ResponseEntity.notFound().build();
        }
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.find(name).orElse(null);
        if (circuitBreaker == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            switch (state.toUpperCase()) {
                case "OPEN":
                    circuitBreaker.transitionToOpenState();
                    response.put("message", "Circuit breaker transitioned to OPEN state");
                    break;
                case "CLOSED":
                    circuitBreaker.transitionToClosedState();
                    response.put("message", "Circuit breaker transitioned to CLOSED state");
                    break;
                case "HALF_OPEN":
                    circuitBreaker.transitionToHalfOpenState();
                    response.put("message", "Circuit breaker transitioned to HALF_OPEN state");
                    break;
                default:
                    response.put("error", "Invalid state. Use: OPEN, CLOSED, or HALF_OPEN");
                    return ResponseEntity.badRequest().body(response);
            }
            
            response.put("name", circuitBreaker.getName());
            response.put("newState", circuitBreaker.getState().name());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Reset circuit breaker metrics
     */
    @PostMapping("/{name}/reset")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker(@PathVariable String name) {
        if (circuitBreakerRegistry == null) {
            return ResponseEntity.notFound().build();
        }
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.find(name).orElse(null);
        if (circuitBreaker == null) {
            return ResponseEntity.notFound().build();
        }
        
        circuitBreaker.reset();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Circuit breaker metrics reset");
        response.put("name", circuitBreaker.getName());
        response.put("state", circuitBreaker.getState().name());
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> getCircuitBreakerInfo(CircuitBreaker circuitBreaker) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", circuitBreaker.getName());
        info.put("state", circuitBreaker.getState().name());
        
        // Metrics
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
        metrics.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
        metrics.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        metrics.put("numberOfNotPermittedCalls", circuitBreaker.getMetrics().getNumberOfNotPermittedCalls());
        metrics.put("numberOfBufferedCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls());
        info.put("metrics", metrics);
        
        return info;
    }
}


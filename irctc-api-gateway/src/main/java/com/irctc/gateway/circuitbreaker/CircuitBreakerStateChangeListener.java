package com.irctc.gateway.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Circuit Breaker State Change Listener
 * 
 * Monitors circuit breaker state changes and logs/metrics them
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class CircuitBreakerStateChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerStateChangeListener.class);
    
    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    private final Map<String, CircuitBreaker.State> circuitStates = new HashMap<>();
    
    @PostConstruct
    public void init() {
        if (circuitBreakerRegistry != null) {
            // Register event listeners for all circuit breakers
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
                circuitBreaker.getEventPublisher()
                    .onStateTransition(this::onStateTransition)
                    .onError(this::onError)
                    .onSuccess(this::onSuccess)
                    .onCallNotPermitted(this::onCallNotPermitted);
                
                circuitStates.put(circuitBreaker.getName(), circuitBreaker.getState());
                logger.info("✅ Registered circuit breaker listener for: {}", circuitBreaker.getName());
            });
        } else {
            logger.warn("⚠️  CircuitBreakerRegistry not available");
        }
    }
    
    private void onStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        String circuitBreakerName = event.getCircuitBreakerName();
        CircuitBreaker.StateTransition stateTransition = event.getStateTransition();
        
        circuitStates.put(circuitBreakerName, stateTransition.getToState());
        
        String message = String.format(
            "🔄 Circuit Breaker '%s' state changed: %s → %s",
            circuitBreakerName,
            stateTransition.getFromState(),
            stateTransition.getToState()
        );
        
        switch (stateTransition.getToState()) {
            case OPEN:
                logger.error("❌ {}", message);
                logger.error("   Circuit breaker opened due to high failure rate. Service unavailable.");
                break;
            case HALF_OPEN:
                logger.warn("⚠️  {}", message);
                logger.warn("   Testing if service has recovered...");
                break;
            case CLOSED:
                logger.info("✅ {}", message);
                logger.info("   Service is healthy. Circuit breaker closed.");
                break;
        }
    }
    
    private void onError(CircuitBreakerEvent event) {
        logger.debug("❌ Circuit breaker '{}' recorded an error", event.getCircuitBreakerName());
    }
    
    private void onSuccess(CircuitBreakerEvent event) {
        logger.debug("✅ Circuit breaker '{}' recorded a success", event.getCircuitBreakerName());
    }
    
    private void onCallNotPermitted(CircuitBreakerEvent event) {
        logger.warn("🚫 Circuit breaker '{}' is OPEN - call not permitted", event.getCircuitBreakerName());
    }
    
    /**
     * Get current state of all circuit breakers
     */
    public Map<String, CircuitBreaker.State> getCircuitStates() {
        return new HashMap<>(circuitStates);
    }
    
    /**
     * Get state of a specific circuit breaker
     */
    public CircuitBreaker.State getCircuitState(String name) {
        return circuitStates.getOrDefault(name, CircuitBreaker.State.CLOSED);
    }
}


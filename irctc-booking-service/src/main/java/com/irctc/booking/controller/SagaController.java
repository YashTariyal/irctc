package com.irctc.booking.controller;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.saga.BookingSagaOrchestrator;
import com.irctc.booking.saga.SagaInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Saga Controller
 * Provides endpoints for saga orchestration
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/saga")
public class SagaController {
    
    @Autowired
    private BookingSagaOrchestrator sagaOrchestrator;
    
    /**
     * Start a booking saga
     */
    @PostMapping("/booking/start")
    public ResponseEntity<SagaInstance> startBookingSaga(@RequestBody SimpleBooking bookingRequest) {
        SagaInstance saga = sagaOrchestrator.startBookingSaga(bookingRequest);
        return ResponseEntity.ok(saga);
    }
    
    /**
     * Get saga by ID
     */
    @GetMapping("/{sagaId}")
    public ResponseEntity<SagaInstance> getSaga(@PathVariable String sagaId) {
        SagaInstance saga = sagaOrchestrator.getSagaById(sagaId);
        return ResponseEntity.ok(saga);
    }
    
    /**
     * Get saga by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<SagaInstance> getSagaByCorrelationId(@PathVariable String correlationId) {
        SagaInstance saga = sagaOrchestrator.getSagaByCorrelationId(correlationId);
        return ResponseEntity.ok(saga);
    }
}


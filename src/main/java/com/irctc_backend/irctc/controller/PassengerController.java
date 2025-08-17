package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.entity.Passenger;
import com.irctc_backend.irctc.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/passengers")
@CrossOrigin(origins = "*")
public class PassengerController {
    
    @Autowired
    private PassengerService passengerService;
    
    @PostMapping
    public ResponseEntity<?> createPassenger(@RequestBody Passenger passenger) {
        try {
            Passenger createdPassenger = passengerService.createPassenger(passenger);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPassenger);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Passenger>> getAllPassengers() {
        List<Passenger> passengers = passengerService.getAllPassengers();
        return ResponseEntity.ok(passengers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPassengerById(@PathVariable Long id) {
        Optional<Passenger> passenger = passengerService.findById(id);
        if (passenger.isPresent()) {
            return ResponseEntity.ok(passenger.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPassengersByUser(@PathVariable Long userId) {
        List<Passenger> passengers = passengerService.getPassengersByUserId(userId);
        return ResponseEntity.ok(passengers);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePassenger(@PathVariable Long id, @RequestBody Passenger passenger) {
        try {
            passenger.setId(id);
            Passenger updatedPassenger = passengerService.updatePassenger(passenger);
            return ResponseEntity.ok(updatedPassenger);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePassenger(@PathVariable Long id) {
        try {
            passengerService.deletePassenger(id);
            return ResponseEntity.ok("Passenger deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 
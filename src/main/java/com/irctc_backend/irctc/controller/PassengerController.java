package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.entity.Passenger;
import com.irctc_backend.irctc.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/passengers")
@CrossOrigin(origins = "*")
@Tag(name = "Passenger Management", description = "APIs for managing passenger information, including CRUD operations and user-specific passenger queries")
public class PassengerController {
    
    @Autowired
    private PassengerService passengerService;
    
    @PostMapping
    @Operation(summary = "Create a new passenger", description = "Creates a new passenger record with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Passenger created successfully",
            content = @Content(schema = @Schema(implementation = Passenger.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation error")
    })
    public ResponseEntity<?> createPassenger(@RequestBody Passenger passenger) {
        try {
            Passenger createdPassenger = passengerService.createPassenger(passenger);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPassenger);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all passengers", description = "Retrieves a list of all passengers in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all passengers",
            content = @Content(schema = @Schema(implementation = Passenger.class)))
    })
    public ResponseEntity<List<Passenger>> getAllPassengers() {
        List<Passenger> passengers = passengerService.getAllPassengers();
        return ResponseEntity.ok(passengers);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get passenger by ID", description = "Retrieves a specific passenger by their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved passenger",
            content = @Content(schema = @Schema(implementation = Passenger.class))),
        @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    public ResponseEntity<?> getPassengerById(
            @Parameter(description = "Unique identifier of the passenger", required = true)
            @PathVariable Long id) {
        Optional<Passenger> passenger = passengerService.findById(id);
        if (passenger.isPresent()) {
            return ResponseEntity.ok(passenger.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get passengers by user ID", description = "Retrieves all passengers associated with a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved passengers for user",
            content = @Content(schema = @Schema(implementation = Passenger.class))),
        @ApiResponse(responseCode = "404", description = "User not found or no passengers associated")
    })
    public ResponseEntity<?> getPassengersByUser(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable Long userId) {
        List<Passenger> passengers = passengerService.getPassengersByUserId(userId);
        return ResponseEntity.ok(passengers);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update passenger", description = "Updates an existing passenger record with new information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Passenger updated successfully",
            content = @Content(schema = @Schema(implementation = Passenger.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation error"),
        @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    public ResponseEntity<?> updatePassenger(
            @Parameter(description = "Unique identifier of the passenger to update", required = true)
            @PathVariable Long id, 
            @RequestBody Passenger passenger) {
        try {
            passenger.setId(id);
            Passenger updatedPassenger = passengerService.updatePassenger(passenger);
            return ResponseEntity.ok(updatedPassenger);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete passenger", description = "Removes a passenger record from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Passenger deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Error during deletion"),
        @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    public ResponseEntity<?> deletePassenger(
            @Parameter(description = "Unique identifier of the passenger to delete", required = true)
            @PathVariable Long id) {
        try {
            passengerService.deletePassenger(id);
            return ResponseEntity.ok("Passenger deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 
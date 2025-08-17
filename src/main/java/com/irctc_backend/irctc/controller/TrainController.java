package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.service.TrainService;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trains")
@CrossOrigin(origins = "*")
@Tag(name = "Train Management", description = "APIs for managing trains, routes, and train information")
public class TrainController {
    
    @Autowired
    private TrainService trainService;
    
    @PostMapping
    public ResponseEntity<?> createTrain(@RequestBody Train train) {
        try {
            Train createdTrain = trainService.createTrain(train);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTrain);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Train>> getAllTrains() {
        List<Train> trains = trainService.getAllTrains();
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Train>> getActiveTrains() {
        List<Train> trains = trainService.getActiveTrains();
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrainById(@PathVariable Long id) {
        Optional<Train> train = trainService.findById(id);
        if (train.isPresent()) {
            return ResponseEntity.ok(train.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/number/{trainNumber}")
    public ResponseEntity<?> getTrainByNumber(@PathVariable String trainNumber) {
        Optional<Train> train = trainService.findByTrainNumber(trainNumber);
        if (train.isPresent()) {
            return ResponseEntity.ok(train.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/type/{trainType}")
    public ResponseEntity<List<Train>> getTrainsByType(@PathVariable Train.TrainType trainType) {
        List<Train> trains = trainService.getTrainsByType(trainType);
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Train>> getTrainsByStatus(@PathVariable Train.TrainStatus status) {
        List<Train> trains = trainService.getTrainsByStatus(status);
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Train>> searchTrains(@RequestParam String searchTerm) {
        List<Train> trains = trainService.searchTrainsByNameOrNumber(searchTerm);
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/route")
    public ResponseEntity<?> getTrainsBetweenStations(
            @RequestParam String sourceStationCode,
            @RequestParam String destStationCode) {
        try {
            List<Train> trains = trainService.getTrainsBetweenStations(sourceStationCode, destStationCode);
            return ResponseEntity.ok(trains);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/route/time")
    public ResponseEntity<?> getTrainsBetweenStationsInTimeRange(
            @RequestParam String sourceStationCode,
            @RequestParam String destStationCode,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            List<Train> trains = trainService.getTrainsBetweenStationsInTimeRange(sourceStationCode, destStationCode, start, end);
            return ResponseEntity.ok(trains);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Invalid time format. Use HH:mm:ss");
        }
    }
    
    @GetMapping("/route/city")
    public ResponseEntity<List<Train>> getTrainsBetweenCities(
            @RequestParam String sourceCity,
            @RequestParam String destCity) {
        List<Train> trains = trainService.getTrainsBetweenCities(sourceCity, destCity);
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/route/state")
    public ResponseEntity<List<Train>> getTrainsBetweenStates(
            @RequestParam String sourceState,
            @RequestParam String destState) {
        List<Train> trains = trainService.getTrainsBetweenStates(sourceState, destState);
        return ResponseEntity.ok(trains);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrain(@PathVariable Long id, @RequestBody Train train) {
        try {
            train.setId(id);
            Train updatedTrain = trainService.updateTrain(train);
            return ResponseEntity.ok(updatedTrain);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTrainStatus(@PathVariable Long id, @RequestParam Train.TrainStatus status) {
        try {
            Train updatedTrain = trainService.updateTrainStatus(id, status);
            return ResponseEntity.ok(updatedTrain);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/running")
    public ResponseEntity<?> updateTrainRunningStatus(@PathVariable Long id, @RequestParam Boolean isRunning) {
        try {
            Train updatedTrain = trainService.updateTrainRunningStatus(id, isRunning);
            return ResponseEntity.ok(updatedTrain);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrain(@PathVariable Long id) {
        try {
            trainService.deleteTrain(id);
            return ResponseEntity.ok("Train deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 
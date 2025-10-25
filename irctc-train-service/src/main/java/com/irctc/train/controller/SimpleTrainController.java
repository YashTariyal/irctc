package com.irctc.train.controller;

import com.irctc.train.entity.SimpleTrain;
import com.irctc.train.service.SimpleTrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trains")
public class SimpleTrainController {

    @Autowired
    private SimpleTrainService trainService;

    @GetMapping
    public ResponseEntity<List<SimpleTrain>> getAllTrains() {
        List<SimpleTrain> trains = trainService.getAllTrains();
        return ResponseEntity.ok(trains);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleTrain> getTrainById(@PathVariable Long id) {
        return trainService.getTrainById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{trainNumber}")
    public ResponseEntity<SimpleTrain> getTrainByNumber(@PathVariable String trainNumber) {
        return trainService.getTrainByNumber(trainNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/route")
    public ResponseEntity<List<SimpleTrain>> searchTrainsByRoute(
            @RequestParam String source,
            @RequestParam String destination) {
        return ResponseEntity.ok(trainService.searchTrains(source, destination));
    }

    @PostMapping
    public ResponseEntity<SimpleTrain> createTrain(@RequestBody SimpleTrain train) {
        SimpleTrain newTrain = trainService.createTrain(train);
        return ResponseEntity.ok(newTrain);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleTrain> updateTrain(@PathVariable Long id, @RequestBody SimpleTrain train) {
        SimpleTrain updatedTrain = trainService.updateTrain(id, train);
        return ResponseEntity.ok(updatedTrain);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrain(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return ResponseEntity.noContent().build();
    }

    // ===== ADVANCED TRAIN APIs =====
    
    @GetMapping("/active")
    public ResponseEntity<List<SimpleTrain>> getActiveTrains() {
        List<SimpleTrain> activeTrains = trainService.getAllTrains().stream()
                .filter(train -> "ACTIVE".equals(train.getStatus()))
                .toList();
        return ResponseEntity.ok(activeTrains);
    }
    
    @GetMapping("/type/{trainType}")
    public ResponseEntity<List<SimpleTrain>> getTrainsByType(@PathVariable String trainType) {
        List<SimpleTrain> trains = trainService.getAllTrains().stream()
                .filter(train -> trainType.equalsIgnoreCase(train.getTrainType()))
                .toList();
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SimpleTrain>> getTrainsByStatus(@PathVariable String status) {
        List<SimpleTrain> trains = trainService.getAllTrains().stream()
                .filter(train -> status.equalsIgnoreCase(train.getStatus()))
                .toList();
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<SimpleTrain>> searchTrains(@RequestParam(required = false) String searchTerm,
                                                         @RequestParam(required = false) String source,
                                                         @RequestParam(required = false) String destination) {
        List<SimpleTrain> trains = trainService.getAllTrains();
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            trains = trains.stream()
                    .filter(train -> train.getTrainName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   train.getTrainNumber().contains(searchTerm))
                    .toList();
        }
        
        if (source != null && !source.isEmpty()) {
            trains = trains.stream()
                    .filter(train -> train.getSourceStation().toLowerCase().contains(source.toLowerCase()))
                    .toList();
        }
        
        if (destination != null && !destination.isEmpty()) {
            trains = trains.stream()
                    .filter(train -> train.getDestinationStation().toLowerCase().contains(destination.toLowerCase()))
                    .toList();
        }
        
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/route")
    public ResponseEntity<List<SimpleTrain>> getTrainsBetweenStations(
            @RequestParam String from,
            @RequestParam String to) {
        List<SimpleTrain> trains = trainService.searchTrains(from, to);
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/route/time")
    public ResponseEntity<List<SimpleTrain>> getTrainsBetweenStationsInTimeRange(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String departureTime,
            @RequestParam(required = false) String arrivalTime) {
        List<SimpleTrain> trains = trainService.searchTrains(from, to);
        
        // Filter by time if provided (simplified implementation)
        if (departureTime != null) {
            trains = trains.stream()
                    .filter(train -> train.getDepartureTime().toString().contains(departureTime))
                    .toList();
        }
        
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/route/city")
    public ResponseEntity<List<SimpleTrain>> getTrainsBetweenCities(
            @RequestParam String fromCity,
            @RequestParam String toCity) {
        // For now, treat city as station (in real implementation, map cities to stations)
        List<SimpleTrain> trains = trainService.searchTrains(fromCity, toCity);
        return ResponseEntity.ok(trains);
    }
    
    @GetMapping("/route/state")
    public ResponseEntity<List<SimpleTrain>> getTrainsBetweenStates(
            @RequestParam String fromState,
            @RequestParam String toState) {
        // For now, treat state as station (in real implementation, map states to stations)
        List<SimpleTrain> trains = trainService.searchTrains(fromState, toState);
        return ResponseEntity.ok(trains);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<SimpleTrain> updateTrainStatus(@PathVariable Long id, @RequestParam String status) {
        SimpleTrain train = trainService.getTrainById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        train.setStatus(status);
        SimpleTrain updatedTrain = trainService.updateTrain(id, train);
        return ResponseEntity.ok(updatedTrain);
    }
    
    @PutMapping("/{id}/running")
    public ResponseEntity<SimpleTrain> updateTrainRunningStatus(@PathVariable Long id, @RequestParam Boolean isRunning) {
        SimpleTrain train = trainService.getTrainById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        // In real implementation, update running status
        SimpleTrain updatedTrain = trainService.updateTrain(id, train);
        return ResponseEntity.ok(updatedTrain);
    }
}

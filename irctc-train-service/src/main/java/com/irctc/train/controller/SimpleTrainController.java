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

    @GetMapping("/search")
    public ResponseEntity<List<SimpleTrain>> searchTrains(
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
}

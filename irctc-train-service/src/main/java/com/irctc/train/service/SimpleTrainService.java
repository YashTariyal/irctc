package com.irctc.train.service;

import com.irctc.train.entity.SimpleTrain;
import com.irctc.train.repository.SimpleTrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SimpleTrainService {

    @Autowired
    private SimpleTrainRepository trainRepository;

    @Autowired(required = false)
    private TrainCacheService cacheService;

    public List<SimpleTrain> getAllTrains() {
        return trainRepository.findAll();
    }

    public Optional<SimpleTrain> getTrainById(Long id) {
        // Try cache first
        if (cacheService != null) {
            Optional<SimpleTrain> cached = cacheService.getCachedTrain(id);
            if (cached.isPresent()) {
                return cached;
            }
        }
        
        Optional<SimpleTrain> train = trainRepository.findById(id);
        
        // Cache the result
        if (train.isPresent() && cacheService != null) {
            cacheService.cacheTrain(id, train.get());
        }
        
        return train;
    }

    public Optional<SimpleTrain> getTrainByNumber(String trainNumber) {
        // Try cache first
        if (cacheService != null) {
            Optional<SimpleTrain> cached = cacheService.getCachedTrainByNumber(trainNumber);
            if (cached.isPresent()) {
                return cached;
            }
        }
        
        Optional<SimpleTrain> train = trainRepository.findByTrainNumber(trainNumber);
        
        // Cache the result
        if (train.isPresent() && cacheService != null) {
            cacheService.cacheTrainByNumber(trainNumber, train.get());
        }
        
        return train;
    }

    public List<SimpleTrain> searchTrains(String source, String destination) {
        // Try cache first
        if (cacheService != null) {
            Optional<List<SimpleTrain>> cached = cacheService.getCachedTrainSearch(source, destination);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        
        List<SimpleTrain> trains = trainRepository.findBySourceStationAndDestinationStation(source, destination);
        
        // Cache the result
        if (cacheService != null) {
            cacheService.cacheTrainSearch(source, destination, trains);
        }
        
        return trains;
    }

    public SimpleTrain createTrain(SimpleTrain train) {
        // createdAt will be set automatically by @PrePersist
        train.setStatus("ACTIVE");
        SimpleTrain saved = trainRepository.save(train);
        
        // Cache the new train
        if (cacheService != null) {
            cacheService.cacheTrain(saved.getId(), saved);
            cacheService.cacheTrainByNumber(saved.getTrainNumber(), saved);
        }
        
        return saved;
    }

    public SimpleTrain updateTrain(Long id, SimpleTrain trainDetails) {
        SimpleTrain train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));

        train.setTrainName(trainDetails.getTrainName());
        train.setSourceStation(trainDetails.getSourceStation());
        train.setDestinationStation(trainDetails.getDestinationStation());
        train.setDepartureTime(trainDetails.getDepartureTime());
        train.setArrivalTime(trainDetails.getArrivalTime());
        train.setTrainType(trainDetails.getTrainType());
        train.setTrainClass(trainDetails.getTrainClass());
        train.setBaseFare(trainDetails.getBaseFare());
        train.setTotalSeats(trainDetails.getTotalSeats());
        train.setAvailableSeats(trainDetails.getAvailableSeats());
        train.setAmenities(trainDetails.getAmenities());
        train.setRouteDescription(trainDetails.getRouteDescription());
        train.setDistance(trainDetails.getDistance());
        train.setDuration(trainDetails.getDuration());

        SimpleTrain saved = trainRepository.save(train);
        
        // Invalidate and refresh cache
        if (cacheService != null) {
            cacheService.invalidateTrain(id);
            cacheService.cacheTrain(id, saved);
            cacheService.cacheTrainByNumber(saved.getTrainNumber(), saved);
        }
        
        return saved;
    }

    public void deleteTrain(Long id) {
        SimpleTrain train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        train.setStatus("INACTIVE");
        trainRepository.save(train);
        
        // Invalidate cache
        if (cacheService != null) {
            cacheService.invalidateTrain(id);
        }
    }
}

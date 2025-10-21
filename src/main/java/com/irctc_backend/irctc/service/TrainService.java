package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.annotation.ExecutionTime;
import com.irctc_backend.irctc.entity.Station;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.repository.StationRepository;
import com.irctc_backend.irctc.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainService.class);
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired(required = false)
    private CacheService cacheService;
    
    @ExecutionTime("Create Train")
    @CacheEvict(value = {"train-schedules", "stations"}, allEntries = true)
    public Train createTrain(Train train) {
        // Validate source and destination stations
        if (train.getSourceStation().getId().equals(train.getDestinationStation().getId())) {
            throw new RuntimeException("Source and destination stations cannot be the same");
        }
        
        // Check if train number already exists
        if (trainRepository.existsByTrainNumber(train.getTrainNumber())) {
            throw new RuntimeException("Train number already exists");
        }
        
        // Calculate journey duration if not provided
        if (train.getJourneyDuration() == null) {
            train.setJourneyDuration(calculateJourneyDuration(train.getDepartureTime(), train.getArrivalTime()));
        }
        
        return trainRepository.save(train);
    }
    
    @Cacheable(value = "train-schedules", key = "#trainNumber")
    public Optional<Train> findByTrainNumber(String trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber);
    }
    
    @ExecutionTime("Search Trains with Caching")
    public List<Train> searchTrainsWithCache(String sourceStationCode, String destinationStationCode, String journeyDate) {
        if (cacheService == null) {
            // No cache service available, search database directly
            logger.info("No cache service available, querying database directly");
            return trainRepository.findAll(); // Simplified for now
        }
        
        String cacheKey = CacheService.Keys.TRAIN_SEARCH + sourceStationCode + ":" + destinationStationCode + ":" + journeyDate;
        
        // Try to get from cache first
        Optional<Object> cachedResult = cacheService.get(cacheKey);
        if (cachedResult.isPresent()) {
            logger.info("Cache hit for train search: {}", cacheKey);
            @SuppressWarnings("unchecked")
            List<Train> cachedTrains = (List<Train>) cachedResult.get();
            return cachedTrains;
        }
        
        // If not in cache, search database
        logger.info("Cache miss for train search: {}, querying database", cacheKey);
        List<Train> trains = trainRepository.findAll(); // Simplified for now
        
        // Cache the result for 30 minutes
        cacheService.put(cacheKey, trains, 1800);
        
        return trains;
    }
    
    @Cacheable(value = "train-schedules", key = "#id")
    public Optional<Train> findById(Long id) {
        return trainRepository.findById(id);
    }
    
    @Cacheable(value = "train-schedules", key = "'all-trains'")
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }
    
    @Cacheable(value = "train-schedules", key = "'active-trains'")
    public List<Train> getActiveTrains() {
        return trainRepository.findByIsRunning(true);
    }
    
    @Cacheable(value = "train-schedules", key = "#trainType")
    public List<Train> getTrainsByType(Train.TrainType trainType) {
        return trainRepository.findByTrainType(trainType);
    }
    
    @Cacheable(value = "train-schedules", key = "#status")
    public List<Train> getTrainsByStatus(Train.TrainStatus status) {
        return trainRepository.findByStatus(status);
    }
    
    @Cacheable(value = "train-schedules", key = "#searchTerm")
    public List<Train> searchTrainsByNameOrNumber(String searchTerm) {
        return trainRepository.findByNameOrNumberContaining(searchTerm);
    }
    
    @ExecutionTime("Search Trains Between Stations")
    @Cacheable(value = "train-schedules", key = "'stations-' + #sourceStationCode + '-' + #destStationCode")
    public List<Train> getTrainsBetweenStations(String sourceStationCode, String destStationCode) {
        Station sourceStation = stationRepository.findByStationCode(sourceStationCode)
            .orElseThrow(() -> new RuntimeException("Source station not found"));
        
        Station destStation = stationRepository.findByStationCode(destStationCode)
            .orElseThrow(() -> new RuntimeException("Destination station not found"));
        
        return trainRepository.findActiveTrainsBetweenStations(sourceStation, destStation);
    }
    
    @Cacheable(value = "train-schedules", key = "'time-range-' + #sourceStationCode + '-' + #destStationCode + '-' + #startTime + '-' + #endTime")
    public List<Train> getTrainsBetweenStationsInTimeRange(String sourceStationCode, String destStationCode, 
                                                          LocalTime startTime, LocalTime endTime) {
        Station sourceStation = stationRepository.findByStationCode(sourceStationCode)
            .orElseThrow(() -> new RuntimeException("Source station not found"));
        
        Station destStation = stationRepository.findByStationCode(destStationCode)
            .orElseThrow(() -> new RuntimeException("Destination station not found"));
        
        return trainRepository.findTrainsBetweenStationsInTimeRange(sourceStation, destStation, startTime, endTime);
    }
    
    @Cacheable(value = "train-schedules", key = "'cities-' + #sourceCity + '-' + #destCity")
    public List<Train> getTrainsBetweenCities(String sourceCity, String destCity) {
        return trainRepository.findActiveTrainsBetweenCities(sourceCity, destCity);
    }
    
    @Cacheable(value = "train-schedules", key = "'states-' + #sourceState + '-' + #destState")
    public List<Train> getTrainsBetweenStates(String sourceState, String destState) {
        return trainRepository.findActiveTrainsBetweenStates(sourceState, destState);
    }
    
    public Train updateTrain(Train train) {
        Train existingTrain = trainRepository.findById(train.getId())
            .orElseThrow(() -> new RuntimeException("Train not found"));
        
        // Update fields
        existingTrain.setTrainName(train.getTrainName());
        existingTrain.setSourceStation(train.getSourceStation());
        existingTrain.setDestinationStation(train.getDestinationStation());
        existingTrain.setDepartureTime(train.getDepartureTime());
        existingTrain.setArrivalTime(train.getArrivalTime());
        existingTrain.setJourneyDuration(calculateJourneyDuration(train.getDepartureTime(), train.getArrivalTime()));
        existingTrain.setTotalDistance(train.getTotalDistance());
        existingTrain.setTrainType(train.getTrainType());
        existingTrain.setStatus(train.getStatus());
        existingTrain.setIsRunning(train.getIsRunning());
        
        return trainRepository.save(existingTrain);
    }
    
    public Train updateTrainStatus(Long trainId, Train.TrainStatus status) {
        Train train = trainRepository.findById(trainId)
            .orElseThrow(() -> new RuntimeException("Train not found"));
        
        train.setStatus(status);
        if (status == Train.TrainStatus.CANCELLED || status == Train.TrainStatus.INACTIVE) {
            train.setIsRunning(false);
        }
        
        return trainRepository.save(train);
    }
    
    public Train updateTrainRunningStatus(Long trainId, Boolean isRunning) {
        Train train = trainRepository.findById(trainId)
            .orElseThrow(() -> new RuntimeException("Train not found"));
        
        train.setIsRunning(isRunning);
        if (!isRunning) {
            train.setStatus(Train.TrainStatus.INACTIVE);
        } else {
            train.setStatus(Train.TrainStatus.ACTIVE);
        }
        
        return trainRepository.save(train);
    }
    
    public void deleteTrain(Long trainId) {
        trainRepository.deleteById(trainId);
    }
    
    private Integer calculateJourneyDuration(LocalTime departureTime, LocalTime arrivalTime) {
        int departureMinutes = departureTime.getHour() * 60 + departureTime.getMinute();
        int arrivalMinutes = arrivalTime.getHour() * 60 + arrivalTime.getMinute();
        
        int duration = arrivalMinutes - departureMinutes;
        if (duration < 0) {
            duration += 24 * 60; // Add 24 hours if arrival is next day
        }
        
        return duration;
    }
} 
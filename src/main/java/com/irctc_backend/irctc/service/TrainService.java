package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.annotation.ExecutionTime;
import com.irctc_backend.irctc.entity.Station;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.repository.StationRepository;
import com.irctc_backend.irctc.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @ExecutionTime("Create Train")
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
    
    public Optional<Train> findByTrainNumber(String trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber);
    }
    
    public Optional<Train> findById(Long id) {
        return trainRepository.findById(id);
    }
    
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }
    
    public List<Train> getActiveTrains() {
        return trainRepository.findByIsRunning(true);
    }
    
    public List<Train> getTrainsByType(Train.TrainType trainType) {
        return trainRepository.findByTrainType(trainType);
    }
    
    public List<Train> getTrainsByStatus(Train.TrainStatus status) {
        return trainRepository.findByStatus(status);
    }
    
    public List<Train> searchTrainsByNameOrNumber(String searchTerm) {
        return trainRepository.findByNameOrNumberContaining(searchTerm);
    }
    
    @ExecutionTime("Search Trains Between Stations")
    public List<Train> getTrainsBetweenStations(String sourceStationCode, String destStationCode) {
        Station sourceStation = stationRepository.findByStationCode(sourceStationCode)
            .orElseThrow(() -> new RuntimeException("Source station not found"));
        
        Station destStation = stationRepository.findByStationCode(destStationCode)
            .orElseThrow(() -> new RuntimeException("Destination station not found"));
        
        return trainRepository.findActiveTrainsBetweenStations(sourceStation, destStation);
    }
    
    public List<Train> getTrainsBetweenStationsInTimeRange(String sourceStationCode, String destStationCode, 
                                                          LocalTime startTime, LocalTime endTime) {
        Station sourceStation = stationRepository.findByStationCode(sourceStationCode)
            .orElseThrow(() -> new RuntimeException("Source station not found"));
        
        Station destStation = stationRepository.findByStationCode(destStationCode)
            .orElseThrow(() -> new RuntimeException("Destination station not found"));
        
        return trainRepository.findTrainsBetweenStationsInTimeRange(sourceStation, destStation, startTime, endTime);
    }
    
    public List<Train> getTrainsBetweenCities(String sourceCity, String destCity) {
        return trainRepository.findActiveTrainsBetweenCities(sourceCity, destCity);
    }
    
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
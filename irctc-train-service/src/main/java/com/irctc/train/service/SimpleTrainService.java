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

    public List<SimpleTrain> getAllTrains() {
        return trainRepository.findAll();
    }

    public Optional<SimpleTrain> getTrainById(Long id) {
        return trainRepository.findById(id);
    }

    public Optional<SimpleTrain> getTrainByNumber(String trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber);
    }

    public List<SimpleTrain> searchTrains(String source, String destination) {
        return trainRepository.findBySourceStationAndDestinationStation(source, destination);
    }

    public SimpleTrain createTrain(SimpleTrain train) {
        // createdAt will be set automatically by @PrePersist
        train.setStatus("ACTIVE");
        return trainRepository.save(train);
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

        return trainRepository.save(train);
    }

    public void deleteTrain(Long id) {
        SimpleTrain train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        train.setStatus("INACTIVE");
        trainRepository.save(train);
    }
}

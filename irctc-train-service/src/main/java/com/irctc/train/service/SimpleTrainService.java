package com.irctc.train.service;

import com.irctc.train.entity.SimpleTrain;
import com.irctc.train.tenant.TenantContext;
import com.irctc.train.repository.SimpleTrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "all-trains")
    public List<SimpleTrain> getAllTrains() {
        List<SimpleTrain> trains = trainRepository.findAll();
        // Filter by tenant if context is set
        if (TenantContext.hasTenant()) {
            String tenantId = TenantContext.getTenantId();
            return trains.stream()
                .filter(t -> tenantId.equals(t.getTenantId()))
                .toList();
        }
        return trains;
    }

    @Cacheable(value = "trains", key = "#id", unless = "#result.isEmpty()")
    public Optional<SimpleTrain> getTrainById(Long id) {
        Optional<SimpleTrain> train = trainRepository.findById(id);
        // Validate tenant access
        if (train.isPresent() && TenantContext.hasTenant()) {
            SimpleTrain t = train.get();
            if (!TenantContext.getTenantId().equals(t.getTenantId())) {
                return Optional.empty();
            }
        }
        return train;
    }

    @Cacheable(value = "trains-by-number", key = "#trainNumber", unless = "#result.isEmpty()")
    public Optional<SimpleTrain> getTrainByNumber(String trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber);
    }

    @Cacheable(value = "train-search", key = "#source + ':' + #destination")
    public List<SimpleTrain> searchTrains(String source, String destination) {
        List<SimpleTrain> trains = trainRepository.findBySourceStationAndDestinationStation(source, destination);
        // Filter by tenant if context is set
        if (TenantContext.hasTenant()) {
            String tenantId = TenantContext.getTenantId();
            return trains.stream()
                .filter(t -> tenantId.equals(t.getTenantId()))
                .toList();
        }
        return trains;
    }

    @CacheEvict(value = {"all-trains", "train-search"}, allEntries = true)
    public SimpleTrain createTrain(SimpleTrain train) {
        // Set tenant ID from context
        if (TenantContext.hasTenant()) {
            train.setTenantId(TenantContext.getTenantId());
        }
        // createdAt will be set automatically by @PrePersist
        train.setStatus("ACTIVE");
        SimpleTrain saved = trainRepository.save(train);
        return saved;
    }

    @CacheEvict(value = {"trains", "trains-by-number", "all-trains", "train-search"}, 
                key = "#id", allEntries = false)
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
        return saved;
    }

    @CacheEvict(value = {"trains", "trains-by-number", "all-trains", "train-search"}, 
                key = "#id", allEntries = false)
    public void deleteTrain(Long id) {
        SimpleTrain train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        train.setStatus("INACTIVE");
        trainRepository.save(train);
    }
}

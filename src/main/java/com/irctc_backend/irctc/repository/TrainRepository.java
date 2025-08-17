package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Station;
import com.irctc_backend.irctc.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    
    Optional<Train> findByTrainNumber(String trainNumber);
    
    boolean existsByTrainNumber(String trainNumber);
    
    List<Train> findBySourceStation(Station sourceStation);
    
    List<Train> findByDestinationStation(Station destinationStation);
    
    List<Train> findBySourceStationAndDestinationStation(Station sourceStation, Station destinationStation);
    
    List<Train> findByTrainType(Train.TrainType trainType);
    
    List<Train> findByStatus(Train.TrainStatus status);
    
    List<Train> findByIsRunning(Boolean isRunning);
    
    @Query("SELECT t FROM Train t WHERE t.sourceStation = :source AND t.destinationStation = :destination AND t.isRunning = true")
    List<Train> findActiveTrainsBetweenStations(@Param("source") Station source, @Param("destination") Station destination);
    
    @Query("SELECT t FROM Train t WHERE t.sourceStation = :source AND t.destinationStation = :destination AND t.departureTime >= :startTime AND t.departureTime <= :endTime AND t.isRunning = true")
    List<Train> findTrainsBetweenStationsInTimeRange(
        @Param("source") Station source, 
        @Param("destination") Station destination,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
    
    @Query("SELECT t FROM Train t WHERE t.trainName LIKE %:name% OR t.trainNumber LIKE %:name%")
    List<Train> findByNameOrNumberContaining(@Param("name") String name);
    
    @Query("SELECT t FROM Train t WHERE t.sourceStation.city = :sourceCity AND t.destinationStation.city = :destCity AND t.isRunning = true")
    List<Train> findActiveTrainsBetweenCities(@Param("sourceCity") String sourceCity, @Param("destCity") String destCity);
    
    @Query("SELECT t FROM Train t WHERE t.sourceStation.state = :sourceState AND t.destinationStation.state = :destState AND t.isRunning = true")
    List<Train> findActiveTrainsBetweenStates(@Param("sourceState") String sourceState, @Param("destState") String destState);
} 
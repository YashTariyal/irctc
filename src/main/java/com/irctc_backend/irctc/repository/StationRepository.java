package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    
    Optional<Station> findByStationCode(String stationCode);
    
    Optional<Station> findByStationName(String stationName);
    
    boolean existsByStationCode(String stationCode);
    
    List<Station> findByCity(String city);
    
    List<Station> findByState(String state);
    
    List<Station> findByZone(String zone);
    
    List<Station> findByIsActive(Boolean isActive);
    
    List<Station> findByStationType(Station.StationType stationType);
    
    @Query("SELECT s FROM Station s WHERE s.stationName LIKE %:name% OR s.stationCode LIKE %:name%")
    List<Station> findByNameOrCodeContaining(@Param("name") String name);
    
    @Query("SELECT s FROM Station s WHERE s.city LIKE %:city%")
    List<Station> findByCityContaining(@Param("city") String city);
    
    @Query("SELECT DISTINCT s.city FROM Station s WHERE s.isActive = true ORDER BY s.city")
    List<String> findAllActiveCities();
    
    @Query("SELECT DISTINCT s.state FROM Station s WHERE s.isActive = true ORDER BY s.state")
    List<String> findAllActiveStates();
} 
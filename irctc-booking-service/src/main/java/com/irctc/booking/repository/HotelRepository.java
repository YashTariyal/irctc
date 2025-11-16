package com.irctc.booking.repository;

import com.irctc.booking.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByLocationAndIsActiveTrue(String location);
    
    List<Hotel> findByNearestStationCodeAndIsActiveTrue(String stationCode);
    
    @Query("SELECT h FROM Hotel h WHERE h.location = :location AND h.isActive = true AND h.availableRooms > 0")
    List<Hotel> findAvailableHotelsByLocation(@Param("location") String location);
    
    @Query("SELECT h FROM Hotel h WHERE h.location = :location AND h.isActive = true " +
           "AND h.pricePerNight BETWEEN :minPrice AND :maxPrice AND h.availableRooms > 0")
    List<Hotel> findHotelsByLocationAndPriceRange(
        @Param("location") String location,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );
    
    @Query("SELECT h FROM Hotel h WHERE h.nearestStationCode = :stationCode AND h.isActive = true " +
           "AND h.rating >= :minRating AND h.availableRooms > 0 ORDER BY h.rating DESC")
    List<Hotel> findRecommendedHotelsByStation(
        @Param("stationCode") String stationCode,
        @Param("minRating") BigDecimal minRating
    );
}


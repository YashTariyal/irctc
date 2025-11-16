package com.irctc.booking.repository;

import com.irctc.booking.entity.HotelBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelBookingRepository extends JpaRepository<HotelBooking, Long> {
    Optional<HotelBooking> findByBookingReference(String bookingReference);
    
    List<HotelBooking> findByUserId(Long userId);
    
    List<HotelBooking> findByUserIdAndStatus(Long userId, String status);
    
    List<HotelBooking> findByTrainBookingId(Long trainBookingId);
    
    @Query("SELECT hb FROM HotelBooking hb WHERE hb.hotelId = :hotelId " +
           "AND ((hb.checkInDate <= :checkOutDate AND hb.checkOutDate >= :checkInDate) " +
           "OR (hb.checkInDate <= :checkInDate AND hb.checkOutDate >= :checkOutDate)) " +
           "AND hb.status IN ('CONFIRMED', 'CHECKED_IN')")
    List<HotelBooking> findConflictingBookings(
        @Param("hotelId") Long hotelId,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate
    );
    
    @Query("SELECT hb FROM HotelBooking hb WHERE hb.userId = :userId " +
           "AND hb.status = 'CONFIRMED' ORDER BY hb.checkInDate DESC")
    List<HotelBooking> findRecentBookingsByUser(@Param("userId") Long userId);
}


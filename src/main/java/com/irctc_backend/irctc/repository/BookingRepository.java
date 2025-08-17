package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Booking;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByPnrNumber(String pnrNumber);
    
    boolean existsByPnrNumber(String pnrNumber);
    
    List<Booking> findByUser(User user);
    
    List<Booking> findByTrain(Train train);
    
    List<Booking> findByJourneyDate(LocalDate journeyDate);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    List<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus);
    
    List<Booking> findByQuotaType(Booking.QuotaType quotaType);
    
    List<Booking> findByIsTatkal(Boolean isTatkal);
    
    List<Booking> findByIsCancelled(Boolean isCancelled);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.journeyDate >= :date")
    List<Booking> findUpcomingBookingsByUser(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.journeyDate < :date")
    List<Booking> findPastBookingsByUser(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.train = :train AND b.journeyDate = :date")
    List<Booking> findBookingsByTrainAndDate(@Param("train") Train train, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    List<Booking> findBookingsInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = 'CONFIRMED' AND b.journeyDate >= :date")
    List<Booking> findConfirmedUpcomingBookingsByUser(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.train = :train AND b.journeyDate = :date AND b.status = 'CONFIRMED'")
    Long countConfirmedBookingsByTrainAndDate(@Param("train") Train train, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.pnrNumber LIKE %:pnr%")
    List<Booking> findByPnrNumberContaining(@Param("pnr") String pnr);
} 
package com.irctc.booking.service;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimpleBookingService {

    @Autowired
    private SimpleBookingRepository bookingRepository;

    @Autowired(required = false)
    private BookingCacheService cacheService;

    public List<SimpleBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<SimpleBooking> getBookingById(Long id) {
        // Try cache first
        if (cacheService != null) {
            Optional<SimpleBooking> cached = cacheService.getCachedBooking(id);
            if (cached.isPresent()) {
                return cached;
            }
        }
        
        Optional<SimpleBooking> booking = bookingRepository.findById(id);
        
        // Cache the result
        if (booking.isPresent() && cacheService != null) {
            cacheService.cacheBooking(id, booking.get());
        }
        
        return booking;
    }

    public Optional<SimpleBooking> getBookingByPnr(String pnrNumber) {
        // Try cache first
        if (cacheService != null) {
            Optional<SimpleBooking> cached = cacheService.getCachedBookingByPnr(pnrNumber);
            if (cached.isPresent()) {
                return cached;
            }
        }
        
        Optional<SimpleBooking> booking = bookingRepository.findByPnrNumber(pnrNumber);
        
        // Cache the result
        if (booking.isPresent() && cacheService != null) {
            cacheService.cacheBookingByPnr(pnrNumber, booking.get());
        }
        
        return booking;
    }

    public List<SimpleBooking> getBookingsByUserId(Long userId) {
        // Try cache first
        if (cacheService != null) {
            Optional<List<SimpleBooking>> cached = cacheService.getCachedUserBookings(userId);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        
        List<SimpleBooking> bookings = bookingRepository.findByUserId(userId);
        
        // Cache the result
        if (cacheService != null) {
            cacheService.cacheUserBookings(userId, bookings);
        }
        
        return bookings;
    }

    public SimpleBooking createBooking(SimpleBooking booking) {
        booking.setPnrNumber(generatePnr());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        booking.setCreatedAt(LocalDateTime.now());
        SimpleBooking saved = bookingRepository.save(booking);
        
        // Invalidate user bookings cache
        if (cacheService != null) {
            cacheService.invalidateUserBookings(saved.getUserId());
        }
        
        return saved;
    }

    public SimpleBooking updateBooking(Long id, SimpleBooking bookingDetails) {
        SimpleBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking", id));

        booking.setUserId(bookingDetails.getUserId());
        booking.setTrainId(bookingDetails.getTrainId());
        booking.setStatus(bookingDetails.getStatus());
        booking.setTotalFare(bookingDetails.getTotalFare());
        booking.setPassengers(bookingDetails.getPassengers());

        SimpleBooking saved = bookingRepository.save(booking);
        
        // Invalidate cache
        if (cacheService != null) {
            cacheService.invalidateBooking(id, saved.getPnrNumber());
            cacheService.invalidateUserBookings(saved.getUserId());
        }
        
        return saved;
    }

    public void cancelBooking(Long id) {
        SimpleBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking", id));
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        
        // Invalidate cache
        if (cacheService != null) {
            cacheService.invalidateBooking(id, booking.getPnrNumber());
            cacheService.invalidateUserBookings(booking.getUserId());
        }
    }

    private String generatePnr() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}

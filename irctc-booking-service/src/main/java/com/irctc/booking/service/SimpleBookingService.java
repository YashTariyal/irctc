package com.irctc.booking.service;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.metrics.BookingMetrics;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.websocket.BookingStatusHandler;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class SimpleBookingService {

    @Autowired
    private SimpleBookingRepository bookingRepository;

    @Autowired(required = false)
    private BookingCacheService cacheService;

    @Autowired(required = false)
    private BookingMetrics bookingMetrics;

    @Autowired(required = false)
    private BookingStatusHandler bookingStatusHandler;

    public List<SimpleBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Bulkhead(name = "booking-query", type = Bulkhead.Type.SEMAPHORE)
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

    @Bulkhead(name = "booking-query", type = Bulkhead.Type.SEMAPHORE)
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

    @Bulkhead(name = "booking-creation", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "booking-creation")
    public SimpleBooking createBooking(SimpleBooking booking) {
        Timer.Sample timer = bookingMetrics != null ? bookingMetrics.startBookingCreationTimer() : null;
        
        try {
            booking.setPnrNumber(generatePnr());
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus("CONFIRMED");
            booking.setCreatedAt(LocalDateTime.now());
            SimpleBooking saved = bookingRepository.save(booking);
            
            // Metrics
            if (bookingMetrics != null) {
                bookingMetrics.incrementBookingsCreated();
                bookingMetrics.incrementBookingsConfirmed();
                bookingMetrics.recordRevenue(saved.getTotalFare());
                if (saved.getPassengers() != null) {
                    bookingMetrics.incrementPassengersBooked(saved.getPassengers().size());
                }
                if (timer != null) {
                    bookingMetrics.recordBookingCreationTime(timer);
                }
            }
            
            // Invalidate user bookings cache
            if (cacheService != null) {
                cacheService.invalidateUserBookings(saved.getUserId());
            }
            
            // Broadcast WebSocket update asynchronously
            if (bookingStatusHandler != null) {
                broadcastBookingUpdateAsync(saved);
            }
            
            return saved;
        } catch (Exception e) {
            if (bookingMetrics != null) {
                bookingMetrics.incrementBookingsFailed();
            }
            throw e;
        }
    }

    @Bulkhead(name = "booking-update", type = Bulkhead.Type.SEMAPHORE)
    public SimpleBooking updateBooking(Long id, SimpleBooking bookingDetails) {
        SimpleBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking", id));

        String oldStatus = booking.getStatus();
        
        booking.setUserId(bookingDetails.getUserId());
        booking.setTrainId(bookingDetails.getTrainId());
        booking.setStatus(bookingDetails.getStatus());
        booking.setTotalFare(bookingDetails.getTotalFare());
        booking.setPassengers(bookingDetails.getPassengers());

        SimpleBooking saved = bookingRepository.save(booking);
        
        // Metrics - track status changes
        if (bookingMetrics != null && !oldStatus.equals(saved.getStatus())) {
            if ("CONFIRMED".equals(saved.getStatus())) {
                bookingMetrics.incrementBookingsConfirmed();
            } else if ("CANCELLED".equals(saved.getStatus())) {
                bookingMetrics.incrementBookingsCancelled();
            }
        }
        
        // Invalidate cache
        if (cacheService != null) {
            cacheService.invalidateBooking(id, saved.getPnrNumber());
            cacheService.invalidateUserBookings(saved.getUserId());
        }
        
        // Broadcast WebSocket update asynchronously
        if (bookingStatusHandler != null) {
            broadcastBookingUpdateAsync(saved);
        }
        
        return saved;
    }

    public void cancelBooking(Long id) {
        Timer.Sample timer = bookingMetrics != null ? bookingMetrics.startBookingCancellationTimer() : null;
        
        try {
            SimpleBooking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Booking", id));
            booking.setStatus("CANCELLED");
            SimpleBooking saved = bookingRepository.save(booking);
            
            // Metrics
            if (bookingMetrics != null) {
                bookingMetrics.incrementBookingsCancelled();
                if (timer != null) {
                    bookingMetrics.recordBookingCancellationTime(timer);
                }
            }
            
            // Invalidate cache
            if (cacheService != null) {
                cacheService.invalidateBooking(id, saved.getPnrNumber());
                cacheService.invalidateUserBookings(saved.getUserId());
            }
            
            // Broadcast WebSocket update asynchronously
            if (bookingStatusHandler != null) {
                broadcastBookingUpdateAsync(saved);
            }
        } catch (Exception e) {
            if (bookingMetrics != null) {
                bookingMetrics.incrementBookingsFailed();
            }
            throw e;
        }
    }

    /**
     * Broadcast booking update via WebSocket asynchronously
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> broadcastBookingUpdateAsync(SimpleBooking booking) {
        try {
            if (bookingStatusHandler != null) {
                bookingStatusHandler.broadcastBookingUpdate(booking);
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            return CompletableFuture.failedFuture(e);
        }
    }

    private String generatePnr() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}

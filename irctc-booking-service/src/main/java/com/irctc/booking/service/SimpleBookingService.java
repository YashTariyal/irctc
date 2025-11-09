package com.irctc.booking.service;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.eventtracking.TrackedEventPublisher;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.eventsourcing.BookingEventStore;
import com.irctc.booking.metrics.BookingMetrics;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.websocket.BookingStatusHandler;
import com.irctc.shared.events.BookingEvents;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.irctc.booking.lock.DistributedLock;
import com.irctc.booking.performance.PerformanceMonitoringService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class SimpleBookingService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleBookingService.class);

    @Autowired
    private SimpleBookingRepository bookingRepository;
    
    @Autowired(required = false)
    private PerformanceMonitoringService performanceMonitoringService;

    @Autowired(required = false)
    private BookingCacheService cacheService;

    @Autowired(required = false)
    private BookingMetrics bookingMetrics;

    @Autowired(required = false)
    private BookingStatusHandler bookingStatusHandler;
    
    @Autowired(required = false)
    private BookingEventStore eventStore;
    
    @Autowired(required = false)
    private TrackedEventPublisher trackedEventPublisher;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    public List<SimpleBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Bulkhead(name = "booking-query", type = Bulkhead.Type.SEMAPHORE)
    @Cacheable(value = "bookings", key = "#id", unless = "#result.isEmpty()")
    public Optional<SimpleBooking> getBookingById(Long id) {
        logger.debug("Fetching booking from database: {}", id);
        return bookingRepository.findById(id);
    }

    @Cacheable(value = "bookings-by-pnr", key = "#pnrNumber", unless = "#result.isEmpty()")
    public Optional<SimpleBooking> getBookingByPnr(String pnrNumber) {
        logger.debug("Fetching booking from database by PNR: {}", pnrNumber);
        return bookingRepository.findByPnrNumber(pnrNumber);
    }

    @Bulkhead(name = "booking-query", type = Bulkhead.Type.SEMAPHORE)
    @Cacheable(value = "bookings-by-user", key = "#userId")
    public List<SimpleBooking> getBookingsByUserId(Long userId) {
        logger.debug("Fetching bookings from database for user: {}", userId);
        return bookingRepository.findByUserId(userId);
    }

    @Bulkhead(name = "booking-creation", type = Bulkhead.Type.SEMAPHORE)
    @CacheEvict(value = {"bookings-by-user"}, key = "#booking.userId", allEntries = false)
    @DistributedLock(key = "booking:#{#booking.trainId}", timeout = 30, waitTime = 5)
    public SimpleBooking createBooking(SimpleBooking booking) {
        Timer.Sample timer = bookingMetrics != null ? bookingMetrics.startBookingCreationTimer() : null;
        long startTime = System.currentTimeMillis();
        
        try {
            booking.setPnrNumber(generatePnr());
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus("CONFIRMED");
            booking.setCreatedAt(LocalDateTime.now());
            SimpleBooking saved = bookingRepository.save(booking);
            
            // Store event in event store (Event Sourcing)
            if (eventStore != null) {
                try {
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("bookingId", saved.getId());
                    eventData.put("userId", saved.getUserId());
                    eventData.put("trainId", saved.getTrainId());
                    eventData.put("pnrNumber", saved.getPnrNumber());
                    eventData.put("totalFare", saved.getTotalFare());
                    eventData.put("status", saved.getStatus());
                    // Convert LocalDateTime to String to avoid serialization issues
                    eventData.put("bookingTime", saved.getBookingTime() != null ? saved.getBookingTime().toString() : null);
                    
                    eventStore.appendEvent(
                        saved.getId().toString(),
                        "BOOKING_CREATED",
                        eventData,
                        UUID.randomUUID().toString(), // correlationId
                        saved.getUserId().toString()
                    );
                    logger.info("📝 Event stored: BOOKING_CREATED for booking: {}", saved.getId());
                } catch (Exception e) {
                    logger.error("Failed to store event for booking: {}", saved.getId(), e);
                    // Don't fail the booking creation if event storage fails
                }
            }
            
            // Publish event to Kafka (for event tracking and downstream services)
            try {
                BookingEvents.BookingCreatedEvent bookingEvent = new BookingEvents.BookingCreatedEvent(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getTrainId(),
                    saved.getPnrNumber(),
                    saved.getTotalFare(),
                    saved.getPassengers() != null ? saved.getPassengers().size() : 0,
                    saved.getBookingTime()
                );
                
                // Use TrackedEventPublisher if available (for event tracking), fallback to kafkaTemplate
                if (trackedEventPublisher != null) {
                    trackedEventPublisher.publishEvent("booking-created", bookingEvent);
                    logger.info("📤 Published booking created event (tracked) for booking: {}", saved.getId());
                } else if (kafkaTemplate != null) {
                    kafkaTemplate.send("booking-created", bookingEvent);
                    logger.info("📤 Published booking created event for booking: {}", saved.getId());
                } else {
                    logger.debug("Kafka not available, skipping event publication for booking: {}", saved.getId());
                }
            } catch (Exception e) {
                logger.error("Failed to publish booking created event for booking: {}", saved.getId(), e);
                // Don't fail the booking creation if event publishing fails
            }
            
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
            
            // Record slow query if applicable
            long duration = System.currentTimeMillis() - startTime;
            if (performanceMonitoringService != null && duration > 1000) {
                performanceMonitoringService.recordSlowQuery(
                    "createBooking", duration, "booking_creation"
                );
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
    @CacheEvict(value = {"bookings", "bookings-by-pnr", "bookings-by-user"}, 
                key = "#id", allEntries = false)
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
        
        // Store event in event store (Event Sourcing)
        if (eventStore != null && !oldStatus.equals(saved.getStatus())) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("bookingId", saved.getId());
            eventData.put("oldStatus", oldStatus);
            eventData.put("newStatus", saved.getStatus());
            eventData.put("userId", saved.getUserId());
            
            String eventType = "BOOKING_STATUS_CHANGED";
            if ("CANCELLED".equals(saved.getStatus())) {
                eventType = "BOOKING_CANCELLED";
            } else if ("CONFIRMED".equals(saved.getStatus())) {
                eventType = "BOOKING_CONFIRMED";
            }
            
            eventStore.appendEvent(
                saved.getId().toString(),
                eventType,
                eventData,
                UUID.randomUUID().toString(),
                saved.getUserId().toString()
            );
            logger.info("📝 Event stored: {} for booking: {}", eventType, saved.getId());
        }
        
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
            
            // Store event in event store (Event Sourcing)
            if (eventStore != null) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("bookingId", saved.getId());
                eventData.put("userId", saved.getUserId());
                eventData.put("pnrNumber", saved.getPnrNumber());
                eventData.put("cancellationTime", LocalDateTime.now());
                
                eventStore.appendEvent(
                    saved.getId().toString(),
                    "BOOKING_CANCELLED",
                    eventData,
                    UUID.randomUUID().toString(),
                    saved.getUserId().toString()
                );
                logger.info("📝 Event stored: BOOKING_CANCELLED for booking: {}", saved.getId());
            }
            
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
            logger.error("Error cancelling booking: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Actually delete a booking from the database (hard delete)
     * This will trigger @PreRemove audit listener
     * Use with caution - this permanently removes the booking
     */
    public void deleteBooking(Long id) {
        try {
            SimpleBooking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Booking", id));
            
            // Invalidate cache before deletion
            if (cacheService != null) {
                cacheService.invalidateBooking(id, booking.getPnrNumber());
                cacheService.invalidateUserBookings(booking.getUserId());
            }
            
            // Actually delete the entity - this will trigger @PreRemove audit listener
            bookingRepository.delete(booking);
            
            logger.info("✅ Booking deleted: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting booking: {}", id, e);
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

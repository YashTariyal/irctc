package com.irctc.booking.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom Business Metrics for Booking Service
 * 
 * Tracks key business KPIs and operational metrics:
 * - Booking creation, cancellation, confirmation rates
 * - Booking processing times
 * - Revenue metrics
 * - Active bookings count
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class BookingMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter bookingsCreated;
    private final Counter bookingsCancelled;
    private final Counter bookingsConfirmed;
    private final Counter bookingsFailed;
    private final Counter revenueGenerated;
    private final Counter passengersBooked;

    // Timers
    private final Timer bookingCreationTime;
    private final Timer bookingCancellationTime;
    private final Timer bookingRetrievalTime;

    // Gauges
    private final AtomicLong activeBookings;
    private final AtomicLong todayBookings;
    private final AtomicLong totalRevenue;

    public BookingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.bookingsCreated = Counter.builder("bookings.created")
                .description("Total number of bookings created")
                .tag("status", "created")
                .register(meterRegistry);

        this.bookingsCancelled = Counter.builder("bookings.cancelled")
                .description("Total number of bookings cancelled")
                .tag("status", "cancelled")
                .register(meterRegistry);

        this.bookingsConfirmed = Counter.builder("bookings.confirmed")
                .description("Total number of bookings confirmed")
                .tag("status", "confirmed")
                .register(meterRegistry);

        this.bookingsFailed = Counter.builder("bookings.failed")
                .description("Total number of failed bookings")
                .tag("status", "failed")
                .register(meterRegistry);

        this.revenueGenerated = Counter.builder("revenue.generated")
                .description("Total revenue generated from bookings")
                .baseUnit("INR")
                .register(meterRegistry);

        this.passengersBooked = Counter.builder("passengers.booked")
                .description("Total number of passengers booked")
                .register(meterRegistry);

        // Initialize timers
        this.bookingCreationTime = Timer.builder("bookings.creation.time")
                .description("Time taken to create a booking")
                .register(meterRegistry);

        this.bookingCancellationTime = Timer.builder("bookings.cancellation.time")
                .description("Time taken to cancel a booking")
                .register(meterRegistry);

        this.bookingRetrievalTime = Timer.builder("bookings.retrieval.time")
                .description("Time taken to retrieve booking details")
                .register(meterRegistry);

        // Initialize gauges
        this.activeBookings = new AtomicLong(0);
        Gauge.builder("bookings.active", activeBookings, AtomicLong::get)
                .description("Current number of active bookings")
                .register(meterRegistry);

        this.todayBookings = new AtomicLong(0);
        Gauge.builder("bookings.today", todayBookings, AtomicLong::get)
                .description("Number of bookings created today")
                .register(meterRegistry);

        this.totalRevenue = new AtomicLong(0);
        Gauge.builder("revenue.total", totalRevenue, AtomicLong::doubleValue)
                .description("Total revenue (INR)")
                .baseUnit("INR")
                .register(meterRegistry);
    }

    // Counter methods
    public void incrementBookingsCreated() {
        bookingsCreated.increment();
        activeBookings.incrementAndGet();
        todayBookings.incrementAndGet();
    }

    public void incrementBookingsCancelled() {
        bookingsCancelled.increment();
        activeBookings.decrementAndGet();
    }

    public void incrementBookingsConfirmed() {
        bookingsConfirmed.increment();
    }

    public void incrementBookingsFailed() {
        bookingsFailed.increment();
    }

    public void recordRevenue(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            revenueGenerated.increment(amount.doubleValue());
            totalRevenue.addAndGet(amount.longValue());
        }
    }

    public void incrementPassengersBooked(int count) {
        passengersBooked.increment(count);
    }

    // Timer methods
    public Timer.Sample startBookingCreationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordBookingCreationTime(Timer.Sample sample) {
        sample.stop(bookingCreationTime);
    }

    public Timer.Sample startBookingCancellationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordBookingCancellationTime(Timer.Sample sample) {
        sample.stop(bookingCancellationTime);
    }

    public Timer.Sample startBookingRetrievalTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordBookingRetrievalTime(Timer.Sample sample) {
        sample.stop(bookingRetrievalTime);
    }

    // Gauge methods
    public void setActiveBookings(long count) {
        activeBookings.set(count);
    }

    public void setTodayBookings(long count) {
        todayBookings.set(count);
    }

    public void resetTodayBookings() {
        todayBookings.set(0);
    }
}


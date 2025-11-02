package com.irctc.payment.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom Business Metrics for Payment Service
 * 
 * Tracks payment-related KPIs:
 * - Payment success/failure rates
 * - Refund metrics
 * - Payment processing times
 * - Revenue tracking
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter paymentsProcessed;
    private final Counter paymentsSuccess;
    private final Counter paymentsFailed;
    private final Counter refundsProcessed;
    private final Counter revenueTotal;

    // Timers
    private final Timer paymentProcessingTime;
    private final Timer refundProcessingTime;

    // Gauges
    private final AtomicLong todayPayments;
    private final AtomicLong totalRevenue;

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.paymentsProcessed = Counter.builder("payments.processed")
                .description("Total number of payments processed")
                .register(meterRegistry);

        this.paymentsSuccess = Counter.builder("payments.success")
                .description("Total number of successful payments")
                .tag("status", "success")
                .register(meterRegistry);

        this.paymentsFailed = Counter.builder("payments.failed")
                .description("Total number of failed payments")
                .tag("status", "failed")
                .register(meterRegistry);

        this.refundsProcessed = Counter.builder("payments.refunds")
                .description("Total number of refunds processed")
                .register(meterRegistry);

        this.revenueTotal = Counter.builder("revenue.payments")
                .description("Total revenue from payments")
                .baseUnit("INR")
                .register(meterRegistry);

        // Initialize timers
        this.paymentProcessingTime = Timer.builder("payments.processing.time")
                .description("Time taken to process a payment")
                .register(meterRegistry);

        this.refundProcessingTime = Timer.builder("payments.refund.time")
                .description("Time taken to process a refund")
                .register(meterRegistry);

        // Initialize gauges
        this.todayPayments = new AtomicLong(0);
        Gauge.builder("payments.today", todayPayments, AtomicLong::get)
                .description("Number of payments processed today")
                .register(meterRegistry);

        this.totalRevenue = new AtomicLong(0);
        Gauge.builder("revenue.payments.total", totalRevenue, AtomicLong::doubleValue)
                .description("Total payment revenue (INR)")
                .baseUnit("INR")
                .register(meterRegistry);
    }

    public void incrementPaymentsProcessed() {
        paymentsProcessed.increment();
        todayPayments.incrementAndGet();
    }

    public void incrementPaymentsSuccess() {
        paymentsSuccess.increment();
    }

    public void incrementPaymentsFailed() {
        paymentsFailed.increment();
    }

    public void incrementRefundsProcessed() {
        refundsProcessed.increment();
    }

    public void recordRevenue(Double amount) {
        if (amount != null && amount > 0) {
            revenueTotal.increment(amount);
            totalRevenue.addAndGet(amount.longValue());
        }
    }

    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordPaymentProcessingTime(Timer.Sample sample) {
        sample.stop(paymentProcessingTime);
    }

    public Timer.Sample startRefundProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordRefundProcessingTime(Timer.Sample sample) {
        sample.stop(refundProcessingTime);
    }

    public void resetTodayPayments() {
        todayPayments.set(0);
    }
}


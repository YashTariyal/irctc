package com.irctc_backend.irctc.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics service for tracking ticket confirmation batch processing
 * Provides comprehensive monitoring and analytics for the batch job
 */
@Component
public class TicketConfirmationMetrics {
    
    // Counters for tracking events
    private final Counter confirmationsProcessed;
    private final Counter racConfirmations;
    private final Counter waitlistConfirmations;
    private final Counter batchProcessingRuns;
    private final Counter batchProcessingErrors;
    private final Counter kafkaEventsPublished;
    private final Counter kafkaEventsFailed;
    
    // Timers for tracking performance
    private final Timer batchProcessingTime;
    private final Timer trainProcessingTime;
    private final Timer coachProcessingTime;
    
    // Gauges for current state
    private final AtomicLong activeBatchJobs;
    private final AtomicLong pendingConfirmations;
    private final AtomicLong totalConfirmationsToday;
    
    public TicketConfirmationMetrics(MeterRegistry meterRegistry) {
        
        // Initialize counters
        this.confirmationsProcessed = Counter.builder("ticket.confirmations.processed")
            .description("Total number of tickets confirmed")
            .register(meterRegistry);
            
        this.racConfirmations = Counter.builder("ticket.confirmations.rac")
            .description("Number of RAC tickets confirmed")
            .register(meterRegistry);
            
        this.waitlistConfirmations = Counter.builder("ticket.confirmations.waitlist")
            .description("Number of waitlist tickets confirmed")
            .register(meterRegistry);
            
        this.batchProcessingRuns = Counter.builder("ticket.batch.processing.runs")
            .description("Number of batch processing runs")
            .register(meterRegistry);
            
        this.batchProcessingErrors = Counter.builder("ticket.batch.processing.errors")
            .description("Number of batch processing errors")
            .register(meterRegistry);
            
        this.kafkaEventsPublished = Counter.builder("ticket.kafka.events.published")
            .description("Number of Kafka events published")
            .register(meterRegistry);
            
        this.kafkaEventsFailed = Counter.builder("ticket.kafka.events.failed")
            .description("Number of Kafka events failed")
            .register(meterRegistry);
        
        // Initialize timers
        this.batchProcessingTime = Timer.builder("ticket.batch.processing.time")
            .description("Time taken for batch processing")
            .register(meterRegistry);
            
        this.trainProcessingTime = Timer.builder("ticket.train.processing.time")
            .description("Time taken to process a train")
            .register(meterRegistry);
            
        this.coachProcessingTime = Timer.builder("ticket.coach.processing.time")
            .description("Time taken to process a coach")
            .register(meterRegistry);
        
        // Initialize gauges
        this.activeBatchJobs = meterRegistry.gauge("ticket.batch.active.jobs", new AtomicLong(0));
        this.pendingConfirmations = meterRegistry.gauge("ticket.confirmations.pending", new AtomicLong(0));
        this.totalConfirmationsToday = meterRegistry.gauge("ticket.confirmations.today", new AtomicLong(0));
    }
    
    /**
     * Record a successful confirmation
     */
    public void recordConfirmation(String confirmationType) {
        confirmationsProcessed.increment();
        
        if ("RAC".equals(confirmationType)) {
            racConfirmations.increment();
        } else if ("WAITLIST".equals(confirmationType)) {
            waitlistConfirmations.increment();
        }
        
        totalConfirmationsToday.incrementAndGet();
    }
    
    /**
     * Record batch processing run
     */
    public void recordBatchProcessingRun() {
        batchProcessingRuns.increment();
    }
    
    /**
     * Record batch processing error
     */
    public void recordBatchProcessingError() {
        batchProcessingErrors.increment();
    }
    
    /**
     * Record Kafka event published
     */
    public void recordKafkaEventPublished() {
        kafkaEventsPublished.increment();
    }
    
    /**
     * Record Kafka event failed
     */
    public void recordKafkaEventFailed() {
        kafkaEventsFailed.increment();
    }
    
    /**
     * Record batch processing time
     */
    public void recordBatchProcessingTime(Duration duration) {
        batchProcessingTime.record(duration);
    }
    
    /**
     * Record train processing time
     */
    public void recordTrainProcessingTime(Duration duration) {
        trainProcessingTime.record(duration);
    }
    
    /**
     * Record coach processing time
     */
    public void recordCoachProcessingTime(Duration duration) {
        coachProcessingTime.record(duration);
    }
    
    /**
     * Set active batch jobs count
     */
    public void setActiveBatchJobs(long count) {
        activeBatchJobs.set(count);
    }
    
    /**
     * Set pending confirmations count
     */
    public void setPendingConfirmations(long count) {
        pendingConfirmations.set(count);
    }
    
    /**
     * Increment active batch jobs
     */
    public void incrementActiveBatchJobs() {
        activeBatchJobs.incrementAndGet();
    }
    
    /**
     * Decrement active batch jobs
     */
    public void decrementActiveBatchJobs() {
        activeBatchJobs.decrementAndGet();
    }
    
    /**
     * Get current metrics summary
     */
    public BatchProcessingMetricsSummary getMetricsSummary() {
        return BatchProcessingMetricsSummary.builder()
            .totalConfirmationsProcessed(confirmationsProcessed.count())
            .racConfirmations(racConfirmations.count())
            .waitlistConfirmations(waitlistConfirmations.count())
            .batchProcessingRuns(batchProcessingRuns.count())
            .batchProcessingErrors(batchProcessingErrors.count())
            .kafkaEventsPublished(kafkaEventsPublished.count())
            .kafkaEventsFailed(kafkaEventsFailed.count())
            .averageBatchProcessingTime(batchProcessingTime.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .averageTrainProcessingTime(trainProcessingTime.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .averageCoachProcessingTime(coachProcessingTime.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .activeBatchJobs(activeBatchJobs.get())
            .pendingConfirmations(pendingConfirmations.get())
            .totalConfirmationsToday(totalConfirmationsToday.get())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Reset daily counters (called at midnight)
     */
    public void resetDailyCounters() {
        totalConfirmationsToday.set(0);
    }
    
    /**
     * Metrics summary data class
     */
    @lombok.Data
    @lombok.Builder
    public static class BatchProcessingMetricsSummary {
        private double totalConfirmationsProcessed;
        private double racConfirmations;
        private double waitlistConfirmations;
        private double batchProcessingRuns;
        private double batchProcessingErrors;
        private double kafkaEventsPublished;
        private double kafkaEventsFailed;
        private double averageBatchProcessingTime;
        private double averageTrainProcessingTime;
        private double averageCoachProcessingTime;
        private long activeBatchJobs;
        private long pendingConfirmations;
        private long totalConfirmationsToday;
        private LocalDateTime timestamp;
        
        /**
         * Calculate success rate
         */
        public double getSuccessRate() {
            if (batchProcessingRuns == 0) return 0.0;
            return ((batchProcessingRuns - batchProcessingErrors) / batchProcessingRuns) * 100.0;
        }
        
        /**
         * Calculate Kafka success rate
         */
        public double getKafkaSuccessRate() {
            double total = kafkaEventsPublished + kafkaEventsFailed;
            if (total == 0) return 0.0;
            return (kafkaEventsPublished / total) * 100.0;
        }
        
        /**
         * Calculate average confirmations per batch run
         */
        public double getAverageConfirmationsPerRun() {
            if (batchProcessingRuns == 0) return 0.0;
            return totalConfirmationsProcessed / batchProcessingRuns;
        }
    }
}

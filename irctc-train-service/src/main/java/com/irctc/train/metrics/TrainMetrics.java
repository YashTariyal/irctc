package com.irctc.train.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom Business Metrics for Train Service
 * 
 * Tracks train-related KPIs:
 * - Train search requests
 * - Cache hit/miss rates
 * - Response times
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class TrainMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter trainSearches;
    private final Counter cacheHits;
    private final Counter cacheMisses;

    // Timers
    private final Timer trainSearchTime;
    private final Timer trainRetrievalTime;

    // Gauges
    private final AtomicLong totalTrains;
    private final AtomicLong cacheSize;

    public TrainMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.trainSearches = Counter.builder("trains.searches")
                .description("Total number of train searches")
                .register(meterRegistry);

        this.cacheHits = Counter.builder("trains.cache.hits")
                .description("Number of cache hits")
                .register(meterRegistry);

        this.cacheMisses = Counter.builder("trains.cache.misses")
                .description("Number of cache misses")
                .register(meterRegistry);

        // Initialize timers
        this.trainSearchTime = Timer.builder("trains.search.time")
                .description("Time taken to search for trains")
                .register(meterRegistry);

        this.trainRetrievalTime = Timer.builder("trains.retrieval.time")
                .description("Time taken to retrieve train details")
                .register(meterRegistry);

        // Initialize gauges
        this.totalTrains = new AtomicLong(0);
        Gauge.builder("trains.total", totalTrains, AtomicLong::get)
                .description("Total number of trains")
                .register(meterRegistry);

        this.cacheSize = new AtomicLong(0);
        Gauge.builder("trains.cache.size", cacheSize, AtomicLong::get)
                .description("Current cache size")
                .register(meterRegistry);
    }

    public void incrementTrainSearches() {
        trainSearches.increment();
    }

    public void incrementCacheHits() {
        cacheHits.increment();
    }

    public void incrementCacheMisses() {
        cacheMisses.increment();
    }

    public Timer.Sample startTrainSearchTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordTrainSearchTime(Timer.Sample sample) {
        sample.stop(trainSearchTime);
    }

    public Timer.Sample startTrainRetrievalTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordTrainRetrievalTime(Timer.Sample sample) {
        sample.stop(trainRetrievalTime);
    }

    public void setTotalTrains(long count) {
        totalTrains.set(count);
    }

    public void setCacheSize(long size) {
        cacheSize.set(size);
    }
}


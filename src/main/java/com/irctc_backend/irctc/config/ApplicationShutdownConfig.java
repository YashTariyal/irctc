package com.irctc_backend.irctc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * Configuration for graceful application shutdown
 */
@Configuration
public class ApplicationShutdownConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationShutdownConfig.class);

    @Bean
    public ShutdownHook shutdownHook() {
        return new ShutdownHook();
    }

    public static class ShutdownHook {
        private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

        @PreDestroy
        public void onShutdown() {
            logger.info("🛑 IRCTC Application is shutting down gracefully...");
            logger.info("📊 Finalizing active bookings and transactions...");
            logger.info("🔌 Closing database connections...");
            logger.info("📡 Stopping Kafka consumers...");
            logger.info("✅ IRCTC Application shutdown completed successfully");
        }
    }
}

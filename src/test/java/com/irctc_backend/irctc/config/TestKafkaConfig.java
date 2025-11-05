package com.irctc_backend.irctc.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration for Kafka
 * Provides mock KafkaTemplate for testing when Kafka is not available
 * Uses a mock producer factory that doesn't require a real Kafka broker
 */
@TestConfiguration
public class TestKafkaConfig {

    /**
     * Mock KafkaTemplate for testing
     * Creates a KafkaTemplate with a mock producer factory
     * Note: This will only work if Kafka auto-configuration is disabled or if
     * the service handles null KafkaTemplate gracefully (which it now does)
     */
    @Bean
    @Primary
    public ProducerFactory<String, Object> testObjectProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        // Use a non-existent broker to prevent actual connection attempts
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9099");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 0);
        // Disable batching to avoid blocking
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 0);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "objectKafkaTemplate")
    @Primary
    public KafkaTemplate<String, Object> objectKafkaTemplate() {
        return new KafkaTemplate<>(testObjectProducerFactory());
    }
}


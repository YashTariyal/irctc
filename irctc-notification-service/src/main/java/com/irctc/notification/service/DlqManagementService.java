package com.irctc.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DLQ Management Service
 * 
 * Provides comprehensive DLQ management including:
 * - DLQ message reprocessing
 * - DLQ statistics and monitoring
 * - DLQ message inspection
 * - DLQ alerting
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class DlqManagementService {

    private static final Logger logger = LoggerFactory.getLogger(DlqManagementService.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    // DLQ metrics (optional - only if MeterRegistry is available)
    private Counter dlqMessagesCounter;
    private Counter dlqReprocessedCounter;
    private Counter dlqReprocessFailedCounter;
    private final Map<String, Long> dlqTopicSizes = new ConcurrentHashMap<>();

    public DlqManagementService() {
        // Constructor - metrics initialized in @PostConstruct if available
    }

    @jakarta.annotation.PostConstruct
    public void initMetrics() {
        if (meterRegistry != null) {
            try {
                // Initialize metrics
                this.dlqMessagesCounter = Counter.builder("kafka.dlq.messages.total")
                    .description("Total messages sent to DLQ")
                    .register(meterRegistry);
                
                this.dlqReprocessedCounter = Counter.builder("kafka.dlq.reprocessed.total")
                    .description("Total messages reprocessed from DLQ")
                    .register(meterRegistry);
                
                this.dlqReprocessFailedCounter = Counter.builder("kafka.dlq.reprocess.failed.total")
                    .description("Total failed reprocessing attempts")
                    .register(meterRegistry);
                
                logger.info("✅ DLQ metrics initialized");
            } catch (Exception e) {
                logger.warn("⚠️  Failed to initialize DLQ metrics: {}", e.getMessage());
            }
        } else {
            logger.debug("MeterRegistry not available, DLQ metrics disabled");
        }
    }

    /**
     * Get DLQ statistics for a topic
     */
    public DlqStatistics getDlqStatistics(String dltTopic) {
        DlqStatistics stats = new DlqStatistics();
        stats.setTopic(dltTopic);
        stats.setTimestamp(LocalDateTime.now());
        
        try (KafkaConsumer<String, byte[]> consumer = createDlqConsumer()) {
            consumer.subscribe(Collections.singletonList(dltTopic));
            
            // Poll to get partition information
            consumer.poll(Duration.ofMillis(100));
            
            Set<org.apache.kafka.common.TopicPartition> partitions = consumer.assignment();
            long totalMessages = 0;
            
            for (org.apache.kafka.common.TopicPartition partition : partitions) {
                consumer.seekToEnd(Collections.singleton(partition));
                long endOffset = consumer.position(partition);
                
                consumer.seekToBeginning(Collections.singleton(partition));
                long startOffset = consumer.position(partition);
                
                totalMessages += (endOffset - startOffset);
            }
            
            stats.setMessageCount(totalMessages);
            stats.setPartitionCount(partitions.size());
            dlqTopicSizes.put(dltTopic, totalMessages);
            
            // Update gauge
            if (meterRegistry != null) {
                Gauge.builder("kafka.dlq.messages.count", dltTopic, topic -> dlqTopicSizes.getOrDefault(topic, 0L))
                    .description("Current message count in DLQ")
                    .tag("topic", dltTopic)
                    .register(meterRegistry);
            }
            
        } catch (Exception e) {
            logger.error("Error getting DLQ statistics for topic: {}", dltTopic, e);
            stats.setError(e.getMessage());
        }
        
        return stats;
    }

    /**
     * Reprocess messages from DLQ back to main topic
     */
    public ReprocessResult reprocessDlq(String dltTopic, String mainTopic, int maxRecords) {
        logger.info("🔄 Starting DLQ reprocessing: {} -> {} (max: {})", dltTopic, mainTopic, maxRecords);
        
        ReprocessResult result = new ReprocessResult();
        result.setDltTopic(dltTopic);
        result.setMainTopic(mainTopic);
        result.setStartTime(LocalDateTime.now());
        
        int reprocessed = 0;
        int failed = 0;
        
        try (KafkaConsumer<String, byte[]> consumer = createDlqConsumer()) {
            consumer.subscribe(Collections.singletonList(dltTopic));
            
            while (reprocessed + failed < maxRecords) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
                
                if (records.isEmpty()) {
                    break;
                }
                
                for (ConsumerRecord<String, byte[]> record : records) {
                    try {
                        // Deserialize and republish to main topic
                        Object event = objectMapper.readValue(record.value(), Object.class);
                        kafkaTemplate.send(mainTopic, record.key(), event).get();
                        
                        reprocessed++;
                        if (dlqReprocessedCounter != null) {
                            dlqReprocessedCounter.increment();
                        }
                        
                        logger.debug("✅ Reprocessed message from DLQ: offset={}, key={}", 
                                   record.offset(), record.key());
                        
                        if (reprocessed >= maxRecords) {
                            break;
                        }
                    } catch (Exception e) {
                        failed++;
                        if (dlqReprocessFailedCounter != null) {
                            dlqReprocessFailedCounter.increment();
                        }
                        logger.error("❌ Failed to reprocess DLQ record at offset {}: {}", 
                                   record.offset(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("❌ DLQ reprocessing error", e);
            result.setError(e.getMessage());
        }
        
        result.setEndTime(LocalDateTime.now());
        result.setReprocessedCount(reprocessed);
        result.setFailedCount(failed);
        
        logger.info("✅ DLQ reprocessing completed: {} reprocessed, {} failed from {} to {}", 
                   reprocessed, failed, dltTopic, mainTopic);
        
        return result;
    }

    /**
     * Inspect messages in DLQ (without consuming them)
     */
    public List<DlqMessage> inspectDlqMessages(String dltTopic, int maxMessages) {
        List<DlqMessage> messages = new ArrayList<>();
        
        try (KafkaConsumer<String, byte[]> consumer = createDlqConsumer()) {
            consumer.subscribe(Collections.singletonList(dltTopic));
            
            // Seek to beginning to read from start
            consumer.poll(Duration.ofMillis(100));
            Set<org.apache.kafka.common.TopicPartition> partitions = consumer.assignment();
            for (org.apache.kafka.common.TopicPartition partition : partitions) {
                consumer.seekToBeginning(Collections.singleton(partition));
            }
            
            int count = 0;
            while (count < maxMessages) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
                
                if (records.isEmpty()) {
                    break;
                }
                
                for (ConsumerRecord<String, byte[]> record : records) {
                    DlqMessage dlqMessage = new DlqMessage();
                    dlqMessage.setTopic(record.topic());
                    dlqMessage.setPartition(record.partition());
                    dlqMessage.setOffset(record.offset());
                    dlqMessage.setKey(record.key());
                    dlqMessage.setTimestamp(new Date(record.timestamp()));
                    
                    try {
                        // Try to deserialize the value
                        Object value = objectMapper.readValue(record.value(), Object.class);
                        dlqMessage.setValue(value);
                        dlqMessage.setValueString(objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(value));
                    } catch (Exception e) {
                        dlqMessage.setValueString("Unable to deserialize: " + e.getMessage());
                    }
                    
                    messages.add(dlqMessage);
                    count++;
                    
                    if (count >= maxMessages) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error inspecting DLQ messages for topic: {}", dltTopic, e);
        }
        
        return messages;
    }

    /**
     * Delete messages from DLQ (use with caution!)
     */
    public int deleteDlqMessages(String dltTopic, int maxMessages) {
        logger.warn("⚠️ Deleting messages from DLQ: {} (max: {})", dltTopic, maxMessages);
        
        // Note: Kafka doesn't support direct message deletion
        // This would require consuming and not republishing, or using retention policies
        // For now, we'll just log a warning
        logger.warn("Kafka doesn't support direct message deletion. Use retention policies or consume and discard.");
        
        return 0;
    }

    /**
     * Create a consumer for DLQ topics
     */
    private KafkaConsumer<String, byte[]> createDlqConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-management-" + UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new KafkaConsumer<>(props);
    }

    /**
     * DLQ Statistics DTO
     */
    public static class DlqStatistics {
        private String topic;
        private long messageCount;
        private int partitionCount;
        private LocalDateTime timestamp;
        private String error;

        // Getters and setters
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public long getMessageCount() { return messageCount; }
        public void setMessageCount(long messageCount) { this.messageCount = messageCount; }
        public int getPartitionCount() { return partitionCount; }
        public void setPartitionCount(int partitionCount) { this.partitionCount = partitionCount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * Reprocess Result DTO
     */
    public static class ReprocessResult {
        private String dltTopic;
        private String mainTopic;
        private int reprocessedCount;
        private int failedCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String error;

        // Getters and setters
        public String getDltTopic() { return dltTopic; }
        public void setDltTopic(String dltTopic) { this.dltTopic = dltTopic; }
        public String getMainTopic() { return mainTopic; }
        public void setMainTopic(String mainTopic) { this.mainTopic = mainTopic; }
        public int getReprocessedCount() { return reprocessedCount; }
        public void setReprocessedCount(int reprocessedCount) { this.reprocessedCount = reprocessedCount; }
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * DLQ Message DTO
     */
    public static class DlqMessage {
        private String topic;
        private int partition;
        private long offset;
        private String key;
        private Date timestamp;
        private Object value;
        private String valueString;

        // Getters and setters
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public int getPartition() { return partition; }
        public void setPartition(int partition) { this.partition = partition; }
        public long getOffset() { return offset; }
        public void setOffset(long offset) { this.offset = offset; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        public String getValueString() { return valueString; }
        public void setValueString(String valueString) { this.valueString = valueString; }
    }
}


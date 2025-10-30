package com.irctc.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.shared.events.BookingEvents;
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
import java.util.Collections;
import java.util.Properties;

@Service
public class DlqReprocessorService {

    private static final Logger logger = LoggerFactory.getLogger(DlqReprocessorService.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public int reprocessDlq(String dltTopic, String mainTopic, int maxRecords) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-dlq-reprocessor");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        int reprocessed = 0;
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(dltTopic));

            while (reprocessed < maxRecords) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
                if (records.isEmpty()) {
                    break;
                }
                for (ConsumerRecord<String, byte[]> rec : records) {
                    try {
                        BookingEvents.TicketConfirmationEvent event = objectMapper.readValue(rec.value(), BookingEvents.TicketConfirmationEvent.class);
                        kafkaTemplate.send(mainTopic, event).get();
                        reprocessed++;
                        if (reprocessed >= maxRecords) {
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to reprocess DLQ record at offset {}", rec.offset(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("DLQ reprocessing error", e);
        }
        logger.info("DLQ reprocessing completed. Republished {} records from {} to {}", reprocessed, dltTopic, mainTopic);
        return reprocessed;
    }
}

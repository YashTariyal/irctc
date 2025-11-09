package com.irctc.user.service;

import com.irctc.shared.events.UserEvents;
import com.irctc.user.eventtracking.TrackedEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Event Publisher Service for User Service
 * Now uses TrackedEventPublisher for event tracking
 */
@Service
public class EventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired(required = false)
    private TrackedEventPublisher trackedEventPublisher;

    private static final String USER_TOPIC = "user-events";

    /**
     * Publish user registration event with event tracking
     */
    public void publishUserRegistered(Long userId, String username, String email, 
                                     String firstName, String lastName) {
        UserEvents.UserRegisteredEvent event = new UserEvents.UserRegisteredEvent(
            userId, username, email, firstName, lastName
        );
        
        // Use TrackedEventPublisher if available, fallback to kafkaTemplate
        if (trackedEventPublisher != null) {
            trackedEventPublisher.publishEvent(USER_TOPIC, "user.registered", event)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Failed to publish user registered event: {}", throwable.getMessage());
                        } else {
                            logger.info("User registered event published successfully (tracked)");
                        }
                    });
        } else {
            kafkaTemplate.send(USER_TOPIC, "user.registered", event)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Failed to publish user registered event: {}", throwable.getMessage());
                        } else {
                            logger.info("User registered event published successfully");
                        }
                    });
        }
    }

    /**
     * Publish user login event with event tracking
     */
    public void publishUserLogin(Long userId, String username, String ipAddress, String userAgent) {
        UserEvents.UserLoginEvent event = new UserEvents.UserLoginEvent(
            userId, username, ipAddress, userAgent
        );
        
        // Use TrackedEventPublisher if available, fallback to kafkaTemplate
        if (trackedEventPublisher != null) {
            trackedEventPublisher.publishEvent(USER_TOPIC, "user.login", event)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Failed to publish user login event: {}", throwable.getMessage());
                        } else {
                            logger.info("User login event published successfully (tracked)");
                        }
                    });
        } else {
            kafkaTemplate.send(USER_TOPIC, "user.login", event)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Failed to publish user login event: {}", throwable.getMessage());
                        } else {
                            logger.info("User login event published successfully");
                        }
                    });
        }
    }

    /**
     * Publish user profile updated event with event tracking
     */
    public void publishUserProfileUpdated(Long userId, String updatedFields) {
        UserEvents.UserProfileUpdatedEvent event = new UserEvents.UserProfileUpdatedEvent(
            userId, updatedFields
        );
        
        // Use TrackedEventPublisher if available, fallback to kafkaTemplate
        if (trackedEventPublisher != null) {
            trackedEventPublisher.publishEvent(USER_TOPIC, "user.profile.updated", event)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Failed to publish user profile updated event: {}", throwable.getMessage());
                        } else {
                            logger.info("User profile updated event published successfully (tracked)");
                        }
                    });
        } else {
            kafkaTemplate.send(USER_TOPIC, "user.profile.updated", event)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Failed to publish user profile updated event: {}", throwable.getMessage());
                        } else {
                            logger.info("User profile updated event published successfully");
                        }
                    });
        }
    }
}

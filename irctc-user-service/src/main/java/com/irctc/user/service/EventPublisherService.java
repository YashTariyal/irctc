package com.irctc.user.service;

import com.irctc.shared.events.UserEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Event Publisher Service for User Service
 */
@Service
public class EventPublisherService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_TOPIC = "user-events";

    /**
     * Publish user registration event
     */
    public void publishUserRegistered(Long userId, String username, String email, 
                                     String firstName, String lastName) {
        UserEvents.UserRegisteredEvent event = new UserEvents.UserRegisteredEvent(
            userId, username, email, firstName, lastName
        );
        
        kafkaTemplate.send(USER_TOPIC, "user.registered", event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        System.err.println("Failed to publish user registered event: " + throwable.getMessage());
                    } else {
                        System.out.println("User registered event published successfully");
                    }
                });
    }

    /**
     * Publish user login event
     */
    public void publishUserLogin(Long userId, String username, String ipAddress, String userAgent) {
        UserEvents.UserLoginEvent event = new UserEvents.UserLoginEvent(
            userId, username, ipAddress, userAgent
        );
        
        kafkaTemplate.send(USER_TOPIC, "user.login", event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        System.err.println("Failed to publish user login event: " + throwable.getMessage());
                    } else {
                        System.out.println("User login event published successfully");
                    }
                });
    }

    /**
     * Publish user profile updated event
     */
    public void publishUserProfileUpdated(Long userId, String updatedFields) {
        UserEvents.UserProfileUpdatedEvent event = new UserEvents.UserProfileUpdatedEvent(
            userId, updatedFields
        );
        
        kafkaTemplate.send(USER_TOPIC, "user.profile.updated", event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        System.err.println("Failed to publish user profile updated event: " + throwable.getMessage());
                    } else {
                        System.out.println("User profile updated event published successfully");
                    }
                });
    }
}

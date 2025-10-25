package com.irctc.shared.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * User-related events for Kafka messaging
 */
public class UserEvents {

    public static class UserRegisteredEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("firstName")
        private String firstName;
        
        @JsonProperty("lastName")
        private String lastName;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        
        @JsonProperty("eventType")
        private String eventType = "USER_REGISTERED";

        // Constructors
        public UserRegisteredEvent() {}

        public UserRegisteredEvent(Long userId, String username, String email, String firstName, String lastName) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class UserLoginEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("loginTime")
        private LocalDateTime loginTime;
        
        @JsonProperty("ipAddress")
        private String ipAddress;
        
        @JsonProperty("userAgent")
        private String userAgent;
        
        @JsonProperty("eventType")
        private String eventType = "USER_LOGIN";

        // Constructors
        public UserLoginEvent() {}

        public UserLoginEvent(Long userId, String username, String ipAddress, String userAgent) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.userId = userId;
            this.username = username;
            this.loginTime = LocalDateTime.now();
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class UserProfileUpdatedEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("updatedFields")
        private String updatedFields;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        
        @JsonProperty("eventType")
        private String eventType = "USER_PROFILE_UPDATED";

        // Constructors
        public UserProfileUpdatedEvent() {}

        public UserProfileUpdatedEvent(Long userId, String updatedFields) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.userId = userId;
            this.updatedFields = updatedFields;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUpdatedFields() { return updatedFields; }
        public void setUpdatedFields(String updatedFields) { this.updatedFields = updatedFields; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}

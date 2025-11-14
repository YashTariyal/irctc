package com.irctc.notification.entity;

import com.irctc.notification.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity for user notification preferences
 */
@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notification_prefs_user_id", columnList = "userId"),
    @Index(name = "idx_notification_prefs_tenant_id", columnList = "tenantId")
})
@Data
public class NotificationPreferences implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    // Channel preferences (EMAIL, SMS, WHATSAPP, PUSH)
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;
    
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = true;
    
    @Column(name = "whatsapp_enabled", nullable = false)
    private Boolean whatsappEnabled = false;
    
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;
    
    // Notification type preferences
    @Column(name = "booking_confirmed", nullable = false)
    private Boolean bookingConfirmed = true;
    
    @Column(name = "payment_success", nullable = false)
    private Boolean paymentSuccess = true;
    
    @Column(name = "booking_reminder", nullable = false)
    private Boolean bookingReminder = true;
    
    @Column(name = "cancellation", nullable = false)
    private Boolean cancellation = true;
    
    @Column(name = "modification", nullable = false)
    private Boolean modification = true;
    
    // Quiet hours
    @Column(name = "quiet_hours_enabled", nullable = false)
    private Boolean quietHoursEnabled = false;
    
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart; // e.g., 22:00
    
    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd; // e.g., 08:00
    
    // Digest preferences
    @Column(name = "digest_enabled", nullable = false)
    private Boolean digestEnabled = false;
    
    @Column(name = "digest_frequency") // DAILY, WEEKLY
    private String digestFrequency;
    
    // Channel preferences per notification type (stored as JSON)
    @Column(columnDefinition = "TEXT")
    private String channelPreferences; // JSON: {"BOOKING_CONFIRMED": ["EMAIL", "PUSH"], ...}
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
    
    /**
     * Get channel preferences as map
     */
    public Map<String, java.util.List<String>> getChannelPreferencesMap() {
        if (channelPreferences == null || channelPreferences.isEmpty()) {
            return new HashMap<>();
        }
        // Parse JSON string to Map
        // In production, use Jackson ObjectMapper
        return new HashMap<>();
    }
    
    /**
     * Set channel preferences from map
     */
    public void setChannelPreferencesMap(Map<String, java.util.List<String>> preferences) {
        // Convert Map to JSON string
        // In production, use Jackson ObjectMapper
        this.channelPreferences = "{}";
    }
}


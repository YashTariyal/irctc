package com.irctc.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@EntityListeners(com.irctc.user.audit.EntityAuditListener.class)
public class NotificationPreferences {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private Long userId;

	@Column(nullable = false)
	private boolean emailEnabled = true;

	@Column(nullable = false)
	private boolean smsEnabled = false;

	@Column(nullable = false)
	private boolean pushEnabled = true;

	@Column
	private String quietHours; // e.g. "22:00-07:00" (local time)

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public boolean isEmailEnabled() { return emailEnabled; }
	public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

	public boolean isSmsEnabled() { return smsEnabled; }
	public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }

	public boolean isPushEnabled() { return pushEnabled; }
	public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }

	public String getQuietHours() { return quietHours; }
	public void setQuietHours(String quietHours) { this.quietHours = quietHours; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}



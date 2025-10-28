package com.irctc.user.service;

import com.irctc.user.entity.NotificationPreferences;
import com.irctc.user.repository.NotificationPreferencesRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationPreferencesService {
	private final NotificationPreferencesRepository repository;

	public NotificationPreferencesService(NotificationPreferencesRepository repository) {
		this.repository = repository;
	}

	public NotificationPreferences getOrCreate(Long userId) {
		Optional<NotificationPreferences> existing = repository.findByUserId(userId);
		if (existing.isPresent()) return existing.get();
		NotificationPreferences prefs = new NotificationPreferences();
		prefs.setUserId(userId);
		return repository.save(prefs);
	}

	public NotificationPreferences update(Long userId, NotificationPreferences updated) {
		NotificationPreferences prefs = getOrCreate(userId);
		prefs.setEmailEnabled(updated.isEmailEnabled());
		prefs.setSmsEnabled(updated.isSmsEnabled());
		prefs.setPushEnabled(updated.isPushEnabled());
		prefs.setQuietHours(updated.getQuietHours());
		return repository.save(prefs);
	}
}



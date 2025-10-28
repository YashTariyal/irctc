package com.irctc.user.controller;

import com.irctc.user.entity.NotificationPreferences;
import com.irctc.user.service.NotificationPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
public class NotificationPreferencesController {
	private final NotificationPreferencesService service;

	public NotificationPreferencesController(NotificationPreferencesService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<NotificationPreferences> get(@PathVariable Long userId) {
		return ResponseEntity.ok(service.getOrCreate(userId));
	}

	@PutMapping
	public ResponseEntity<NotificationPreferences> update(@PathVariable Long userId,
			@RequestBody NotificationPreferences body) {
		return ResponseEntity.ok(service.update(userId, body));
	}
}



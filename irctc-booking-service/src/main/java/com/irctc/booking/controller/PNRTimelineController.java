package com.irctc.booking.controller;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pnr")
public class PNRTimelineController {
	private final SimpleBookingRepository bookingRepository;

	public PNRTimelineController(SimpleBookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	@GetMapping("/{pnr}/timeline")
	public ResponseEntity<?> getPNRTimeline(@PathVariable String pnr) {
		return bookingRepository.findByPnrNumber(pnr)
				.map(booking -> ResponseEntity.ok(buildTimeline(booking)))
				.orElse(ResponseEntity.notFound().build());
	}

	private Map<String, Object> buildTimeline(SimpleBooking booking) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		List<Map<String, String>> events = new ArrayList<>();

		Map<String, String> created = new HashMap<>();
		created.put("time", booking.getBookingTime() != null ? booking.getBookingTime().format(fmt) : "-");
		created.put("event", "Booking Created");
		events.add(created);

		Map<String, String> status = new HashMap<>();
		status.put("time", booking.getUpdatedAt() != null ? booking.getUpdatedAt().format(fmt) : "-");
		status.put("event", "Current Status: " + booking.getStatus());
		events.add(status);

		Map<String, Object> payload = new HashMap<>();
		payload.put("pnr", booking.getPnrNumber());
		payload.put("trainId", booking.getTrainId());
		payload.put("userId", booking.getUserId());
		payload.put("timeline", events);
		return payload;
	}
}



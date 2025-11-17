package com.irctc.booking.controller;

import com.irctc.booking.dto.offline.*;
import com.irctc.booking.entity.OfflineAction;
import com.irctc.booking.service.OfflineActionService;
import com.irctc.booking.service.OfflineSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/offline")
public class OfflineSyncController {

    private final OfflineSyncService offlineSyncService;
    private final OfflineActionService offlineActionService;

    public OfflineSyncController(OfflineSyncService offlineSyncService,
                                 OfflineActionService offlineActionService) {
        this.offlineSyncService = offlineSyncService;
        this.offlineActionService = offlineActionService;
    }

    @PostMapping("/sync")
    public ResponseEntity<OfflineSyncResponse> sync(@RequestBody OfflineSyncRequest request) {
        return ResponseEntity.ok(offlineSyncService.generateOfflineBundle(request));
    }

    @GetMapping("/users/{userId}/tickets")
    public ResponseEntity<List<OfflineTicketDTO>> tickets(@PathVariable Long userId) {
        return ResponseEntity.ok(offlineSyncService.getTicketsForUser(userId));
    }

    @PostMapping("/actions")
    public ResponseEntity<OfflineActionResponse> queueAction(@RequestBody OfflineActionRequest request) {
        OfflineAction saved = offlineActionService.queueAction(request);
        return ResponseEntity.ok(map(saved));
    }

    @GetMapping("/actions/pending")
    public ResponseEntity<List<OfflineActionResponse>> pendingActions(@RequestParam Long userId) {
        List<OfflineActionResponse> responses = offlineActionService.getPendingActions(userId).stream()
            .map(this::map)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/actions/process")
    public ResponseEntity<List<OfflineActionResponse>> processActions(@RequestParam(required = false) Long userId) {
        List<OfflineActionResponse> responses = offlineActionService.processPendingActions(userId).stream()
            .map(this::map)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private OfflineActionResponse map(OfflineAction action) {
        OfflineActionResponse response = new OfflineActionResponse();
        response.setId(action.getId());
        response.setUserId(action.getUserId());
        response.setBookingId(action.getBookingId());
        response.setActionType(action.getActionType());
        response.setStatus(action.getStatus());
        response.setFailureReason(action.getFailureReason());
        response.setQueuedAt(action.getQueuedAt());
        response.setProcessedAt(action.getProcessedAt());
        return response;
    }
}


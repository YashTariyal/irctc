package com.irctc.notification.controller;

import com.irctc.notification.service.DlqReprocessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/dlq")
public class DlqController {

    @Autowired
    private DlqReprocessorService reprocessorService;

    @PostMapping("/reprocess")
    public ResponseEntity<?> reprocess(@RequestParam(name = "limit", defaultValue = "50") int limit) {
        int count = reprocessorService.reprocessDlq(
                "ticket-confirmation-events.DLT",
                "ticket-confirmation-events",
                Math.max(1, Math.min(limit, 1000))
        );
        return ResponseEntity.ok("Reprocessed " + count + " DLQ records");
    }
}

package com.irctc.booking.controller;

import com.irctc.booking.dto.QRCodeResponse;
import com.irctc.booking.dto.QRCodeVerificationRequest;
import com.irctc.booking.dto.QRCodeVerificationResponse;
import com.irctc.booking.service.QRCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * QR Code Controller
 * REST API for QR code generation and verification
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/bookings")
public class QRCodeController {
    
    @Autowired
    private QRCodeService qrCodeService;
    
    /**
     * Generate QR code for a booking
     * GET /api/bookings/{id}/qr-code
     */
    @GetMapping("/{id}/qr-code")
    public ResponseEntity<QRCodeResponse> generateQRCode(@PathVariable Long id) {
        QRCodeResponse response = qrCodeService.generateQRCode(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verify QR code
     * POST /api/bookings/verify-qr
     */
    @PostMapping("/verify-qr")
    public ResponseEntity<QRCodeVerificationResponse> verifyQRCode(
            @Valid @RequestBody QRCodeVerificationRequest request) {
        QRCodeVerificationResponse response = qrCodeService.verifyQRCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get QR code status
     * GET /api/bookings/{id}/qr-status
     */
    @GetMapping("/{id}/qr-status")
    public ResponseEntity<QRCodeResponse> getQRCodeStatus(@PathVariable Long id) {
        QRCodeResponse response = qrCodeService.getQRCodeStatus(id);
        return ResponseEntity.ok(response);
    }
}


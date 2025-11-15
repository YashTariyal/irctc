package com.irctc.booking.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.irctc.booking.dto.QRCodeResponse;
import com.irctc.booking.dto.QRCodeVerificationRequest;
import com.irctc.booking.dto.QRCodeVerificationResponse;
import com.irctc.booking.entity.QRCode;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.exception.InvalidRequestException;
import com.irctc.booking.repository.QRCodeRepository;
import com.irctc.booking.tenant.TenantContext;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * QR Code Service
 * Handles QR code generation, encryption, and verification for ticket verification
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class QRCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);
    
    @Autowired
    private QRCodeRepository qrCodeRepository;
    
    @Autowired
    private SimpleBookingService bookingService;
    
    @Value("${qr.code.secret:IRCTC_QR_CODE_SECRET_KEY_FOR_ENCRYPTION_2024}")
    private String qrCodeSecret;
    
    @Value("${qr.code.expiration.hours:24}")
    private int expirationHours;
    
    @Value("${qr.code.width:300}")
    private int qrCodeWidth;
    
    @Value("${qr.code.height:300}")
    private int qrCodeHeight;
    
    /**
     * Generate QR code for a booking
     */
    @Transactional
    public QRCodeResponse generateQRCode(Long bookingId) {
        logger.info("Generating QR code for booking: {}", bookingId);
        
        // Get booking
        SimpleBooking booking = bookingService.getBookingById(bookingId)
            .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        
        // Check if QR code already exists and is active
        Optional<QRCode> existingQR = qrCodeRepository.findByBookingIdAndIsActiveTrue(bookingId);
        if (existingQR.isPresent()) {
            QRCode qr = existingQR.get();
            if (qr.getExpiresAt().isAfter(LocalDateTime.now())) {
                logger.info("Active QR code already exists for booking: {}", bookingId);
                return convertToResponse(qr, generateQRCodeImage(qr.getQrCode()));
            }
        }
        
        // Create encrypted QR code data
        String qrData = createEncryptedQRData(booking);
        
        // Create QR code entity
        QRCode qrCode = new QRCode();
        qrCode.setBookingId(bookingId);
        qrCode.setQrCode(qrData);
        qrCode.setPnrNumber(booking.getPnrNumber());
        qrCode.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        qrCode.setIsActive(true);
        qrCode.setIsVerified(false);
        qrCode.setTenantId(TenantContext.getTenantId());
        
        qrCode = qrCodeRepository.save(qrCode);
        
        // Generate QR code image
        String qrCodeImage = generateQRCodeImage(qrData);
        
        logger.info("QR code generated successfully for booking: {}, PNR: {}", bookingId, booking.getPnrNumber());
        
        return convertToResponse(qrCode, qrCodeImage);
    }
    
    /**
     * Verify QR code
     */
    @Transactional
    public QRCodeVerificationResponse verifyQRCode(QRCodeVerificationRequest request) {
        logger.info("Verifying QR code");
        
        QRCodeVerificationResponse response = new QRCodeVerificationResponse();
        
        try {
            // Decrypt and parse QR code data
            Map<String, String> qrData = decryptQRData(request.getQrCode());
            
            // Find QR code in database
            Optional<QRCode> qrCodeOpt = qrCodeRepository.findByQrCode(request.getQrCode());
            
            if (qrCodeOpt.isEmpty()) {
                // Offline verification - verify using embedded data
                return verifyOffline(qrData, request);
            }
            
            QRCode qrCode = qrCodeOpt.get();
            
            // Check if expired
            if (qrCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                response.setIsValid(false);
                response.setIsExpired(true);
                response.setMessage("QR code has expired");
                return response;
            }
            
            // Check if already verified
            if (qrCode.getIsVerified()) {
                response.setIsValid(true);
                response.setIsAlreadyVerified(true);
                response.setMessage("QR code already verified");
                response.setVerifiedAt(qrCode.getVerifiedAt());
                response.setVerifiedBy(qrCode.getVerifiedBy());
                
                // Get booking status
                Optional<SimpleBooking> booking = bookingService.getBookingById(qrCode.getBookingId());
                if (booking.isPresent()) {
                    response.setBookingId(booking.get().getId());
                    response.setPnrNumber(booking.get().getPnrNumber());
                    response.setBookingStatus(booking.get().getStatus());
                }
                return response;
            }
            
            // Verify booking exists and is valid
            Optional<SimpleBooking> booking = bookingService.getBookingById(qrCode.getBookingId());
            if (booking.isEmpty()) {
                response.setIsValid(false);
                response.setMessage("Booking not found");
                return response;
            }
            
            SimpleBooking bookingEntity = booking.get();
            
            // Check booking status
            if ("CANCELLED".equals(bookingEntity.getStatus())) {
                response.setIsValid(false);
                response.setMessage("Booking has been cancelled");
                response.setBookingId(bookingEntity.getId());
                response.setPnrNumber(bookingEntity.getPnrNumber());
                response.setBookingStatus(bookingEntity.getStatus());
                return response;
            }
            
            // Mark as verified
            qrCode.setIsVerified(true);
            qrCode.setVerifiedAt(LocalDateTime.now());
            qrCode.setVerifiedBy(request.getVerifiedBy() != null ? request.getVerifiedBy() : "SYSTEM");
            qrCodeRepository.save(qrCode);
            
            response.setIsValid(true);
            response.setIsExpired(false);
            response.setIsAlreadyVerified(false);
            response.setMessage("QR code verified successfully");
            response.setBookingId(bookingEntity.getId());
            response.setPnrNumber(bookingEntity.getPnrNumber());
            response.setBookingStatus(bookingEntity.getStatus());
            response.setVerifiedAt(qrCode.getVerifiedAt());
            response.setVerifiedBy(qrCode.getVerifiedBy());
            
            logger.info("QR code verified successfully for booking: {}, PNR: {}", 
                bookingEntity.getId(), bookingEntity.getPnrNumber());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error verifying QR code: {}", e.getMessage(), e);
            response.setIsValid(false);
            response.setMessage("Invalid QR code: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Get QR code status
     */
    @Transactional(readOnly = true)
    public QRCodeResponse getQRCodeStatus(Long bookingId) {
        logger.info("Getting QR code status for booking: {}", bookingId);
        
        QRCode qrCode = qrCodeRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new EntityNotFoundException("QRCode", bookingId));
        
        String qrCodeImage = null;
        if (qrCode.getIsActive() && qrCode.getExpiresAt().isAfter(LocalDateTime.now())) {
            qrCodeImage = generateQRCodeImage(qrCode.getQrCode());
        }
        
        return convertToResponse(qrCode, qrCodeImage);
    }
    
    /**
     * Create encrypted QR code data
     */
    private String createEncryptedQRData(SimpleBooking booking) {
        // Create JSON-like data structure
        Map<String, String> data = new HashMap<>();
        data.put("bookingId", booking.getId().toString());
        data.put("pnrNumber", booking.getPnrNumber());
        data.put("userId", booking.getUserId().toString());
        data.put("trainId", booking.getTrainId().toString());
        data.put("status", booking.getStatus());
        data.put("timestamp", LocalDateTime.now().toString());
        data.put("expiresAt", LocalDateTime.now().plusHours(expirationHours).toString());
        
        // Convert to JSON string
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        
        // Encrypt using JWT
        SecretKey key = Keys.hmacShaKeyFor(qrCodeSecret.getBytes(StandardCharsets.UTF_8));
        String encrypted = Jwts.builder()
            .subject(booking.getId().toString())
            .claim("pnr", booking.getPnrNumber())
            .claim("userId", booking.getUserId())
            .claim("trainId", booking.getTrainId())
            .claim("status", booking.getStatus())
            .claim("timestamp", LocalDateTime.now().toString())
            .claim("expiresAt", LocalDateTime.now().plusHours(expirationHours).toString())
            .signWith(key)
            .compact();
        
        return encrypted;
    }
    
    /**
     * Decrypt QR code data
     */
    private Map<String, String> decryptQRData(String encryptedData) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(qrCodeSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(encryptedData)
                .getPayload();
            
            Map<String, String> data = new HashMap<>();
            data.put("bookingId", claims.getSubject());
            data.put("pnrNumber", claims.get("pnr", String.class));
            data.put("userId", claims.get("userId", String.class));
            data.put("trainId", claims.get("trainId", String.class));
            data.put("status", claims.get("status", String.class));
            data.put("timestamp", claims.get("timestamp", String.class));
            data.put("expiresAt", claims.get("expiresAt", String.class));
            
            return data;
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid QR code data: " + e.getMessage());
        }
    }
    
    /**
     * Verify QR code offline (without database lookup)
     */
    private QRCodeVerificationResponse verifyOffline(Map<String, String> qrData, QRCodeVerificationRequest request) {
        QRCodeVerificationResponse response = new QRCodeVerificationResponse();
        
        try {
            // Check expiration
            String expiresAtStr = qrData.get("expiresAt");
            if (expiresAtStr != null) {
                LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr);
                if (expiresAt.isBefore(LocalDateTime.now())) {
                    response.setIsValid(false);
                    response.setIsExpired(true);
                    response.setMessage("QR code has expired");
                    return response;
                }
            }
            
            // Check booking status
            String status = qrData.get("status");
            if ("CANCELLED".equals(status)) {
                response.setIsValid(false);
                response.setMessage("Booking has been cancelled");
                response.setBookingStatus(status);
                return response;
            }
            
            response.setIsValid(true);
            response.setIsExpired(false);
            response.setMessage("QR code verified (offline mode)");
            response.setBookingId(Long.parseLong(qrData.get("bookingId")));
            response.setPnrNumber(qrData.get("pnrNumber"));
            response.setBookingStatus(status);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error in offline verification: {}", e.getMessage());
            response.setIsValid(false);
            response.setMessage("Offline verification failed: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Generate QR code image as Base64 string
     */
    private String generateQRCodeImage(String data) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrCodeWidth, qrCodeHeight, hints);
            
            BufferedImage image = new BufferedImage(qrCodeWidth, qrCodeHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, qrCodeWidth, qrCodeHeight);
            graphics.setColor(Color.BLACK);
            
            for (int x = 0; x < qrCodeWidth; x++) {
                for (int y = 0; y < qrCodeHeight; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            
            graphics.dispose();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }
    
    /**
     * Convert QRCode entity to QRCodeResponse
     */
    private QRCodeResponse convertToResponse(QRCode qrCode, String qrCodeImage) {
        QRCodeResponse response = new QRCodeResponse();
        response.setId(qrCode.getId());
        response.setBookingId(qrCode.getBookingId());
        response.setQrCode(qrCode.getQrCode());
        response.setPnrNumber(qrCode.getPnrNumber());
        response.setExpiresAt(qrCode.getExpiresAt());
        response.setIsActive(qrCode.getIsActive());
        response.setQrCodeImage(qrCodeImage);
        response.setCreatedAt(qrCode.getCreatedAt());
        return response;
    }
}


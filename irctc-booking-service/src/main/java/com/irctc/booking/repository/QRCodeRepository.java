package com.irctc.booking.repository;

import com.irctc.booking.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    
    /**
     * Find QR code by booking ID
     */
    Optional<QRCode> findByBookingId(Long bookingId);
    
    /**
     * Find QR code by QR code string
     */
    Optional<QRCode> findByQrCode(String qrCode);
    
    /**
     * Find active QR code by booking ID
     */
    Optional<QRCode> findByBookingIdAndIsActiveTrue(Long bookingId);
    
    /**
     * Find QR code by PNR number
     */
    Optional<QRCode> findByPnrNumber(String pnrNumber);
}


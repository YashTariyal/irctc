package com.irctc.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO showing available modification options for a booking
 */
@Data
public class ModificationOptionsResponse {
    private Long bookingId;
    private String currentStatus;
    private boolean canModifyDate;
    private boolean canUpgradeSeat;
    private boolean canChangeRoute;
    private boolean canModifyPassengers;
    
    private List<DateModificationOption> availableDates;
    private List<SeatClassOption> availableSeatClasses;
    private Map<String, BigDecimal> modificationCharges; // e.g., {"dateChange": 200.00, "seatUpgrade": 100.00}
    
    private LocalDateTime lastModificationDate;
    private int modificationCount;
    
    @Data
    public static class DateModificationOption {
        private LocalDateTime date;
        private Long trainId;
        private String trainNumber;
        private BigDecimal fare;
        private BigDecimal modificationCharge;
        private int availableSeats;
    }
    
    @Data
    public static class SeatClassOption {
        private String seatClass;
        private BigDecimal fare;
        private BigDecimal upgradeCharge;
        private int availableSeats;
        private String description;
    }
}


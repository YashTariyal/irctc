package com.irctc.booking.dto.offline;

import lombok.Data;

/**
 * Lightweight passenger payload that can be safely cached offline on mobile devices.
 */
@Data
public class OfflinePassengerDTO {
    private Long passengerId;
    private String name;
    private Integer age;
    private String gender;
    private String seatNumber;
    private String idProofType;
}


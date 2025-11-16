package com.irctc.user.dto;

import lombok.Data;

/**
 * DTO for consent request
 */
@Data
public class ConsentRequest {
    private String consentType; // MARKETING, ANALYTICS, COOKIES, THIRD_PARTY, DATA_PROCESSING
    private Boolean granted; // true to grant, false to withdraw
    private String purpose; // Purpose of consent
    private String version; // Version of consent terms
    private String ipAddress; // Optional: IP address
    private String userAgent; // Optional: User agent
}


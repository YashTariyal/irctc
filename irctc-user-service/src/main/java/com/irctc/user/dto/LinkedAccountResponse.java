package com.irctc.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for linked social account information
 */
@Data
public class LinkedAccountResponse {
    private Long id;
    private String provider; // GOOGLE, FACEBOOK, APPLE
    private String providerEmail;
    private String providerName;
    private String pictureUrl;
    private Boolean active;
    private LocalDateTime linkedAt;
    private LocalDateTime lastUsedAt;
}


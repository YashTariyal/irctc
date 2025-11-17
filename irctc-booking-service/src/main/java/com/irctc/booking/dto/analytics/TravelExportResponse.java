package com.irctc.booking.dto.analytics;

import lombok.Data;

@Data
public class TravelExportResponse {
    private String filename;
    private String format;
    private String contentType;
    private String data;
}


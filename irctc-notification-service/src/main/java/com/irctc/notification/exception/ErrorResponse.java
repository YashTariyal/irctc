package com.irctc.notification.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private Instant timestamp;
    private Integer status;
    private String errorCode;
    private String message;
    private String detail;
    private String path;
    private String method;
    private String correlationId;
    private String traceId;
    private Map<String, Object> errors;
    private String stackTrace;
}


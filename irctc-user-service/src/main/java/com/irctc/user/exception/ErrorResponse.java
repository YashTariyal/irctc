package com.irctc.user.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

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
    
    // Builder pattern
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }
    
    public static class ErrorResponseBuilder {
        private ErrorResponse response = new ErrorResponse();
        
        public ErrorResponseBuilder timestamp(Instant timestamp) { response.timestamp = timestamp; return this; }
        public ErrorResponseBuilder status(Integer status) { response.status = status; return this; }
        public ErrorResponseBuilder errorCode(String errorCode) { response.errorCode = errorCode; return this; }
        public ErrorResponseBuilder message(String message) { response.message = message; return this; }
        public ErrorResponseBuilder detail(String detail) { response.detail = detail; return this; }
        public ErrorResponseBuilder path(String path) { response.path = path; return this; }
        public ErrorResponseBuilder method(String method) { response.method = method; return this; }
        public ErrorResponseBuilder correlationId(String correlationId) { response.correlationId = correlationId; return this; }
        public ErrorResponseBuilder traceId(String traceId) { response.traceId = traceId; return this; }
        public ErrorResponseBuilder errors(Map<String, Object> errors) { response.errors = errors; return this; }
        public ErrorResponseBuilder stackTrace(String stackTrace) { response.stackTrace = stackTrace; return this; }
        public ErrorResponse build() { return response; }
    }
    
    // Getters and Setters
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Map<String, Object> getErrors() { return errors; }
    public void setErrors(Map<String, Object> errors) { this.errors = errors; }
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
}


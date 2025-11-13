package com.irctc.notification.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Enhanced Request/Response Logging Filter
 * 
 * Features:
 * - Complete request/response logging
 * - Sensitive data masking (passwords, tokens, PII)
 * - Correlation ID propagation
 * - Performance metrics (request duration)
 * - Configurable log levels
 * - Request/response size limits
 * - Error tracking
 * 
 * @author IRCTC Development Team
 * @version 2.0.0
 */
@Component
@Order(2) // Execute after CorrelationIdFilter
public class EnhancedRequestResponseLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedRequestResponseLoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Sensitive fields to mask (case-insensitive)
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "password", "pwd", "passwd", "pass",
        "token", "accessToken", "refreshToken", "authToken", "bearer",
        "secret", "apiKey", "apikey", "api_key",
        "authorization", "x-api-key", "x-auth-token",
        "cardNumber", "card_number", "creditCard", "credit_card",
        "cvv", "cvc", "cvv2",
        "pin", "ssn", "socialSecurityNumber",
        "accountNumber", "account_number",
        "routingNumber", "routing_number",
        "email", "phone", "phoneNumber", "phone_number" // PII - mask partially
    );
    
    // Headers to mask
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
        "authorization", "x-api-key", "x-auth-token", "cookie"
    );
    
    // Paths to exclude from logging
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/actuator/health", "/actuator/prometheus", "/actuator/info",
        "/actuator/metrics", "/swagger-ui", "/api-docs"
    );
    
    // Pattern for masking sensitive data in JSON
    private static final Pattern JSON_SENSITIVE_PATTERN = Pattern.compile(
        "(?i)(\"(?:password|token|secret|apiKey|cardNumber|cvv|pin|ssn)\"\\s*:\\s*\")([^\"]+)(\")",
        Pattern.CASE_INSENSITIVE
    );
    
    @Autowired(required = false)
    private MeterRegistry meterRegistry;
    
    @Value("${logging.request-response.enabled:true}")
    private boolean loggingEnabled;
    
    @Value("${logging.request-response.log-request-body:true}")
    private boolean logRequestBody;
    
    @Value("${logging.request-response.log-response-body:true}")
    private boolean logResponseBody;
    
    @Value("${logging.request-response.max-body-size:10000}")
    private int maxBodySize;
    
    @Value("${logging.request-response.slow-request-threshold:2000}")
    private long slowRequestThreshold; // milliseconds
    
    @Value("${logging.request-response.mask-pii:true}")
    private boolean maskPii;
    
    @Override
    public void doFilter(
            jakarta.servlet.ServletRequest request,
            jakarta.servlet.ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        if (!loggingEnabled) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // Skip logging for excluded paths
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }
        
        Instant startTime = Instant.now();
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
        }
        
        // Log request
        if (logRequestBody) {
            logRequest(httpRequest, correlationId);
        } else {
            logRequestBasic(httpRequest, correlationId);
        }
        
        // Wrap request to read body multiple times
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
        
        // Wrap response to capture response body
        CachedBodyHttpServletResponse cachedResponse = new CachedBodyHttpServletResponse(httpResponse);
        
        int statusCode = 200;
        Exception exception = null;
        
        try {
            chain.doFilter(cachedRequest, cachedResponse);
            statusCode = cachedResponse.getStatus();
        } catch (Exception e) {
            exception = e;
            statusCode = 500;
            throw e;
        } finally {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);
            long durationMs = duration.toMillis();
            
            // Log response
            if (logResponseBody) {
                logResponse(httpRequest, cachedResponse, correlationId, durationMs, exception);
            } else {
                logResponseBasic(httpRequest, cachedResponse, correlationId, durationMs, exception);
            }
            
            // Record metrics
            recordMetrics(httpRequest, statusCode, durationMs);
            
            // Log slow requests
            if (durationMs > slowRequestThreshold) {
                logger.warn("⚠️  SLOW REQUEST [{}] {} {} took {}ms (threshold: {}ms)",
                    correlationId, httpRequest.getMethod(), path, durationMs, slowRequestThreshold);
            }
            
            // Write response body back to original response
            byte[] responseBody = cachedResponse.getCachedBody();
            if (responseBody.length > 0) {
                httpResponse.getOutputStream().write(responseBody);
            }
        }
    }
    
    private void logRequest(HttpServletRequest request, String correlationId) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUri = queryString != null ? uri + "?" + queryString : uri;
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            // Log request headers (mask sensitive ones)
            Map<String, String> headers = new LinkedHashMap<>();
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                String headerValue = request.getHeader(headerName);
                if (isSensitiveHeader(headerName)) {
                    headers.put(headerName, maskSensitiveValue(headerValue));
                } else {
                    headers.put(headerName, headerValue);
                }
            });
            
            // Log request body if present
            String requestBody = getRequestBody(request);
            String maskedBody = requestBody != null ? maskSensitiveData(requestBody) : null;
            
            if (maskedBody != null && maskedBody.length() > maxBodySize) {
                maskedBody = maskedBody.substring(0, maxBodySize) + "... (truncated)";
            }
            
            logger.info("📥 REQUEST [{}] {} {} | IP: {} | User-Agent: {} | Headers: {} | Body: {}",
                correlationId, method, fullUri, clientIp, userAgent,
                maskSensitiveData(objectMapper.writeValueAsString(headers)),
                maskedBody != null ? maskedBody : "N/A");
                
        } catch (Exception e) {
            logger.warn("Failed to log request: {}", e.getMessage());
        }
    }
    
    private void logRequestBasic(HttpServletRequest request, String correlationId) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUri = queryString != null ? uri + "?" + queryString : uri;
            String clientIp = getClientIp(request);
            
            logger.info("📥 REQUEST [{}] {} {} | IP: {}",
                correlationId, method, fullUri, clientIp);
                
        } catch (Exception e) {
            logger.warn("Failed to log request: {}", e.getMessage());
        }
    }
    
    private void logResponse(HttpServletRequest request, CachedBodyHttpServletResponse response,
                           String correlationId, long durationMs, Exception exception) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            
            // Get response body
            byte[] responseBody = response.getCachedBody();
            String responseBodyStr = responseBody.length > 0 
                ? new String(responseBody, StandardCharsets.UTF_8)
                : "N/A";
            
            String maskedBody = maskSensitiveData(responseBodyStr);
            if (maskedBody.length() > maxBodySize) {
                maskedBody = maskedBody.substring(0, maxBodySize) + "... (truncated)";
            }
            
            String statusEmoji = getStatusEmoji(status);
            String logLevel = getLogLevel(status);
            
            String message = String.format("%s RESPONSE [%s] %s %s | Status: %d | Duration: %dms | Body: %s",
                statusEmoji, correlationId, method, uri, status, durationMs, maskedBody);
            
            if (exception != null) {
                message += " | Exception: " + exception.getClass().getSimpleName() + " - " + exception.getMessage();
            }
            
            if ("ERROR".equals(logLevel)) {
                logger.error(message);
            } else if ("WARN".equals(logLevel)) {
                logger.warn(message);
            } else {
                logger.info(message);
            }
                    
        } catch (Exception e) {
            logger.warn("Failed to log response: {}", e.getMessage());
        }
    }
    
    private void logResponseBasic(HttpServletRequest request, CachedBodyHttpServletResponse response,
                                String correlationId, long durationMs, Exception exception) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            
            String statusEmoji = getStatusEmoji(status);
            String logLevel = getLogLevel(status);
            
            String message = String.format("%s RESPONSE [%s] %s %s | Status: %d | Duration: %dms",
                statusEmoji, correlationId, method, uri, status, durationMs);
            
            if (exception != null) {
                message += " | Exception: " + exception.getClass().getSimpleName();
            }
            
            if ("ERROR".equals(logLevel)) {
                logger.error(message);
            } else if ("WARN".equals(logLevel)) {
                logger.warn(message);
            } else {
                logger.info(message);
            }
                    
        } catch (Exception e) {
            logger.warn("Failed to log response: {}", e.getMessage());
        }
    }
    
    private String getRequestBody(HttpServletRequest request) {
        if (request instanceof CachedBodyHttpServletRequest) {
            return ((CachedBodyHttpServletRequest) request).getCachedBody();
        }
        return null;
    }
    
    private boolean isSensitiveHeader(String headerName) {
        return SENSITIVE_HEADERS.stream()
            .anyMatch(field -> headerName.toLowerCase().contains(field.toLowerCase()));
    }
    
    private String maskSensitiveValue(String value) {
        if (value == null || value.length() <= 8) {
            return "***";
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }
    
    private String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        try {
            // Try to parse as JSON for better masking
            if (data.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(data);
                maskJsonNode(jsonNode);
                return objectMapper.writeValueAsString(jsonNode);
            }
        } catch (Exception e) {
            // Not JSON, use regex masking
        }
        
        // Regex-based masking for non-JSON data
        String masked = data;
        for (String field : SENSITIVE_FIELDS) {
            // Mask patterns like "password":"value" or password=value
            masked = masked.replaceAll(
                "(?i)([\"']?" + Pattern.quote(field) + "[\"']?\\s*[:=]\\s*[\"']?)([^\"'\\s,}]+)([\"']?)",
                "$1***$3"
            );
        }
        
        return masked;
    }
    
    private void maskJsonNode(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                if (isSensitiveField(key)) {
                    if (value.isTextual()) {
                        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put(key, "***");
                    } else if (value.isNumber()) {
                        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put(key, 0);
                    }
                } else if (value.isObject() || value.isArray()) {
                    maskJsonNode(value);
                }
            });
        } else if (node.isArray()) {
            node.forEach(this::maskJsonNode);
        }
    }
    
    private boolean isSensitiveField(String fieldName) {
        return SENSITIVE_FIELDS.stream()
            .anyMatch(field -> fieldName.toLowerCase().contains(field.toLowerCase()));
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) return "✅";
        if (status >= 300 && status < 400) return "↪️";
        if (status >= 400 && status < 500) return "⚠️";
        if (status >= 500) return "❌";
        return "📤";
    }
    
    private String getLogLevel(int status) {
        if (status >= 500) return "ERROR";
        if (status >= 400) return "WARN";
        return "INFO";
    }
    
    private void recordMetrics(HttpServletRequest request, int statusCode, long durationMs) {
        if (meterRegistry == null) return;
        
        try {
            String method = request.getMethod();
            String path = request.getRequestURI();
            
            // Record request duration
            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("http.request.duration")
                .description("HTTP request duration")
                .tag("method", method)
                .tag("path", sanitizePath(path))
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry));
            
            // Record request count
            meterRegistry.counter("http.request.total",
                "method", method,
                "path", sanitizePath(path),
                "status", String.valueOf(statusCode)
            ).increment();
            
        } catch (Exception e) {
            logger.debug("Failed to record metrics: {}", e.getMessage());
        }
    }
    
    private String sanitizePath(String path) {
        // Replace path parameters with placeholders for better metric aggregation
        return path.replaceAll("/\\d+", "/{id}")
                  .replaceAll("/[a-f0-9-]{36}", "/{uuid}") // UUIDs
                  .replaceAll("/[a-zA-Z0-9]{10,}", "/{param}"); // Long alphanumeric
    }
    
    // Wrapper class to cache request body
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private byte[] cachedBody;
        
        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        }
        
        public String getCachedBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
        
        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(cachedBody);
        }
        
        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
    
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;
        
        public CachedBodyServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }
        
        @Override
        public int read() {
            return buffer.read();
        }
        
        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }
        
        @Override
        public boolean isReady() {
            return true;
        }
        
        @Override
        public void setReadListener(ReadListener listener) {
            // Not needed for synchronous processing
        }
    }
    
    // Wrapper class to cache response body
    private static class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
        private int status = 200;
        
        public CachedBodyHttpServletResponse(HttpServletResponse response) {
            super(response);
        }
        
        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }
        
        @Override
        public int getStatus() {
            return status;
        }
        
        @Override
        public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override
                public void write(int b) {
                    cachedBody.write(b);
                    try {
                        CachedBodyHttpServletResponse.super.getOutputStream().write(b);
                    } catch (IOException e) {
                        // Ignore
                    }
                }
                
                @Override
                public boolean isReady() {
                    return true;
                }
                
                @Override
                public void setWriteListener(WriteListener listener) {
                    // Not needed
                }
            };
        }
        
        public byte[] getCachedBody() {
            return cachedBody.toByteArray();
        }
    }
}


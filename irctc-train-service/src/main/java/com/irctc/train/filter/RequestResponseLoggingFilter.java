package com.irctc.train.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

/**
 * Request/Response Logging Filter
 * 
 * Logs HTTP requests and responses with masking of sensitive data.
 * Sensitive fields (passwords, tokens, etc.) are automatically masked.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
@Order(3) // Execute after CorrelationIdFilter
public class RequestResponseLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Sensitive fields to mask
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password", "token", "secret", "apiKey", "authorization",
        "x-api-key", "x-auth-token", "cardNumber", "cvv", "pin"
    );
    
    // Paths to exclude from logging (e.g., health checks)
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/actuator/health", "/actuator/prometheus", "/actuator/info"
    );
    
    @Override
    public void doFilter(
            jakarta.servlet.ServletRequest request,
            jakarta.servlet.ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // Skip logging for excluded paths
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Log request
        logRequest(httpRequest);
        
        // Wrap request to read body multiple times
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
        
        // Wrap response to capture response body
        CachedBodyHttpServletResponse cachedResponse = new CachedBodyHttpServletResponse(httpResponse);
        
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } finally {
            // Log response
            logResponse(httpRequest, cachedResponse);
            
            // Write response body back to original response
            byte[] responseBody = cachedResponse.getCachedBody();
            if (responseBody.length > 0) {
                httpResponse.getOutputStream().write(responseBody);
            }
        }
    }
    
    private void logRequest(HttpServletRequest request) {
        try {
            String correlationId = MDC.get("correlationId");
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUri = queryString != null ? uri + "?" + queryString : uri;
            
            // Log request headers (mask sensitive ones)
            Map<String, String> headers = new java.util.HashMap<>();
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
            
            logger.info("📥 INCOMING REQUEST [{}] {} {} - Headers: {} - Body: {}",
                correlationId, method, fullUri, 
                maskSensitiveData(objectMapper.writeValueAsString(headers)),
                requestBody != null ? maskSensitiveData(requestBody) : "N/A");
                
        } catch (Exception e) {
            logger.warn("Failed to log request: {}", e.getMessage());
        }
    }
    
    private void logResponse(HttpServletRequest request, CachedBodyHttpServletResponse response) {
        try {
            String correlationId = MDC.get("correlationId");
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            
            // Get response body
            byte[] responseBody = response.getCachedBody();
            String responseBodyStr = responseBody.length > 0 
                ? new String(responseBody, StandardCharsets.UTF_8)
                : "N/A";
            
            logger.info("📤 OUTGOING RESPONSE [{}] {} {} - Status: {} - Body: {}",
                correlationId, method, uri, status,
                responseBodyStr.length() > 1000 
                    ? maskSensitiveData(responseBodyStr.substring(0, 1000)) + "... (truncated)"
                    : maskSensitiveData(responseBodyStr));
                    
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
        return SENSITIVE_FIELDS.stream()
            .anyMatch(field -> headerName.toLowerCase().contains(field.toLowerCase()));
    }
    
    private String maskSensitiveValue(String value) {
        if (value == null || value.length() <= 8) {
            return "***";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }
    
    private String maskSensitiveData(String data) {
        if (data == null) {
            return null;
        }
        
        // Simple masking for JSON data
        for (String field : SENSITIVE_FIELDS) {
            // Mask patterns like "password":"value" or "password":"value"
            data = data.replaceAll(
                "(?i)(\"" + field + "\"\\s*:\\s*\")([^\"]+)(\")",
                "$1***$3"
            );
        }
        
        return data;
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
        
        public CachedBodyHttpServletResponse(HttpServletResponse response) {
            super(response);
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


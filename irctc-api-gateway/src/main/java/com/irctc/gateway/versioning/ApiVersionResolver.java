package com.irctc.gateway.versioning;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API Version Resolver
 * 
 * Resolves API version from multiple sources:
 * 1. URL path (/api/v1/...)
 * 2. Accept header (application/vnd.irctc.v1+json)
 * 3. Custom header (X-API-Version: v1)
 * 4. Query parameter (?version=v1)
 * 
 * Priority: URL > Header > Query Parameter
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class ApiVersionResolver {

    private static final Pattern VERSION_PATTERN = Pattern.compile("/v(\\d+)/");
    private static final Pattern ACCEPT_HEADER_PATTERN = Pattern.compile("application/vnd\\.irctc\\.v(\\d+)\\+json");
    private static final String API_VERSION_HEADER = "X-API-Version";
    private static final String VERSION_QUERY_PARAM = "version";
    
    /**
     * Resolve API version from request
     * 
     * @param request Server HTTP request
     * @return API version (e.g., "v1", "v2") or null if not specified
     */
    public String resolveVersion(ServerHttpRequest request) {
        // Priority 1: URL path
        String version = resolveFromPath(request.getURI().getPath());
        if (version != null) {
            return version;
        }
        
        // Priority 2: Accept header
        List<String> acceptHeaders = request.getHeaders().get("Accept");
        if (acceptHeaders != null) {
            for (String accept : acceptHeaders) {
                version = resolveFromAcceptHeader(accept);
                if (version != null) {
                    return version;
                }
            }
        }
        
        // Priority 3: Custom header
        List<String> versionHeaders = request.getHeaders().get(API_VERSION_HEADER);
        if (versionHeaders != null && !versionHeaders.isEmpty()) {
            version = normalizeVersion(versionHeaders.get(0));
            if (version != null) {
                return version;
            }
        }
        
        // Priority 4: Query parameter
        String versionParam = request.getQueryParams().getFirst(VERSION_QUERY_PARAM);
        if (versionParam != null) {
            version = normalizeVersion(versionParam);
            if (version != null) {
                return version;
            }
        }
        
        return null; // No version specified
    }
    
    /**
     * Resolve version from URL path
     */
    private String resolveFromPath(String path) {
        if (path == null) {
            return null;
        }
        Matcher matcher = VERSION_PATTERN.matcher(path);
        if (matcher.find()) {
            return "v" + matcher.group(1);
        }
        return null;
    }
    
    /**
     * Resolve version from Accept header
     */
    private String resolveFromAcceptHeader(String accept) {
        if (accept == null) {
            return null;
        }
        Matcher matcher = ACCEPT_HEADER_PATTERN.matcher(accept);
        if (matcher.find()) {
            return "v" + matcher.group(1);
        }
        return null;
    }
    
    /**
     * Normalize version string
     */
    private String normalizeVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return null;
        }
        
        version = version.trim().toLowerCase();
        
        // Remove 'v' prefix if present, then add it back
        if (version.startsWith("v")) {
            version = version.substring(1);
        }
        
        // Validate it's a number
        try {
            Integer.parseInt(version);
            return "v" + version;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Get default version if none specified
     */
    public String getDefaultVersion() {
        return "v1";
    }
    
    /**
     * Check if version is supported
     */
    public boolean isVersionSupported(String version) {
        if (version == null) {
            return false;
        }
        // Currently support v1 and v2
        return version.equals("v1") || version.equals("v2");
    }
}


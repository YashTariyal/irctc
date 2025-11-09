package com.irctc.gateway.versioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * API Version Manager
 * 
 * Manages API version information, deprecation status, and migration paths
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class ApiVersionManager {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionManager.class);
    
    private final Map<String, ApiVersionInfo> versionRegistry = new HashMap<>();
    
    @Value("${api.versioning.default-version:v1}")
    private String defaultVersion;
    
    @Value("${api.versioning.enable-deprecation-warnings:true}")
    private boolean enableDeprecationWarnings;
    
    @PostConstruct
    public void init() {
        // Register API versions
        registerVersion("v1", false, null, null, 
            "Initial API version", LocalDate.of(2024, 1, 1));
        
        // v2 is not yet deprecated, but can be configured
        registerVersion("v2", false, null, null, 
            "Enhanced API version with improved responses", LocalDate.of(2024, 6, 1));
        
        logger.info("✅ API Version Manager initialized with {} versions", versionRegistry.size());
    }
    
    /**
     * Register an API version
     */
    public void registerVersion(String version, boolean deprecated, 
                               LocalDate sunsetDate, String replacementVersion,
                               String releaseNotes, LocalDate releaseDate) {
        ApiVersionInfo info = new ApiVersionInfo(version, deprecated);
        info.setSunsetDate(sunsetDate);
        info.setReplacementVersion(replacementVersion);
        info.setReleaseNotes(releaseNotes);
        info.setReleaseDate(releaseDate);
        
        versionRegistry.put(version, info);
        logger.info("Registered API version: {} (deprecated: {})", version, deprecated);
    }
    
    /**
     * Get version information
     */
    public ApiVersionInfo getVersionInfo(String version) {
        return versionRegistry.get(version);
    }
    
    /**
     * Check if version is deprecated
     */
    public boolean isDeprecated(String version) {
        ApiVersionInfo info = versionRegistry.get(version);
        return info != null && info.isDeprecated();
    }
    
    /**
     * Get replacement version for deprecated version
     */
    public String getReplacementVersion(String version) {
        ApiVersionInfo info = versionRegistry.get(version);
        return info != null ? info.getReplacementVersion() : null;
    }
    
    /**
     * Get all registered versions
     */
    public Map<String, ApiVersionInfo> getAllVersions() {
        return new HashMap<>(versionRegistry);
    }
    
    /**
     * Deprecate a version
     */
    public void deprecateVersion(String version, LocalDate sunsetDate, String replacementVersion) {
        ApiVersionInfo info = versionRegistry.get(version);
        if (info != null) {
            info.setDeprecated(true);
            info.setSunsetDate(sunsetDate);
            info.setReplacementVersion(replacementVersion);
            logger.warn("⚠️  API version {} deprecated. Sunset: {}, Replacement: {}", 
                version, sunsetDate, replacementVersion);
        }
    }
    
    /**
     * Get default version
     */
    public String getDefaultVersion() {
        return defaultVersion;
    }
    
    /**
     * Check if deprecation warnings are enabled
     */
    public boolean isDeprecationWarningsEnabled() {
        return enableDeprecationWarnings;
    }
}


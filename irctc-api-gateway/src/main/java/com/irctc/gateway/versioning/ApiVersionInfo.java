package com.irctc.gateway.versioning;

import java.time.LocalDate;

/**
 * API Version Information
 * 
 * Contains metadata about an API version
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class ApiVersionInfo {
    
    private String version;
    private boolean deprecated;
    private LocalDate sunsetDate;
    private String replacementVersion;
    private String releaseNotes;
    private LocalDate releaseDate;
    
    public ApiVersionInfo() {
    }
    
    public ApiVersionInfo(String version, boolean deprecated) {
        this.version = version;
        this.deprecated = deprecated;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }
    
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
    
    public LocalDate getSunsetDate() {
        return sunsetDate;
    }
    
    public void setSunsetDate(LocalDate sunsetDate) {
        this.sunsetDate = sunsetDate;
    }
    
    public String getReplacementVersion() {
        return replacementVersion;
    }
    
    public void setReplacementVersion(String replacementVersion) {
        this.replacementVersion = replacementVersion;
    }
    
    public String getReleaseNotes() {
        return releaseNotes;
    }
    
    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }
    
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}


package com.irctc.gateway.versioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API Version Management Controller
 * 
 * Provides endpoints for querying API version information
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/versions")
public class ApiVersionController {

    @Autowired
    private ApiVersionManager versionManager;
    
    /**
     * Get all API versions
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVersions() {
        Map<String, Object> response = new HashMap<>();
        response.put("versions", versionManager.getAllVersions());
        response.put("defaultVersion", versionManager.getDefaultVersion());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get specific version information
     */
    @GetMapping("/{version}")
    public ResponseEntity<ApiVersionInfo> getVersionInfo(@PathVariable String version) {
        ApiVersionInfo info = versionManager.getVersionInfo(version);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }
    
    /**
     * Get current API version status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVersionStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("defaultVersion", versionManager.getDefaultVersion());
        response.put("supportedVersions", versionManager.getAllVersions().keySet());
        response.put("deprecationWarningsEnabled", versionManager.isDeprecationWarningsEnabled());
        return ResponseEntity.ok(response);
    }
}


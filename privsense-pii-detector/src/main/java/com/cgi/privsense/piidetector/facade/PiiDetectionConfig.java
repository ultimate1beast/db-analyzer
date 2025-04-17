package com.cgi.privsense.piidetector.facade;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for PII detection orchestration.
 * Controls which detection strategies are enabled and their weights.
 */
@Data
@Component
@ConfigurationProperties(prefix = "privsense.pii-detection")
public class PiiDetectionConfig {

    /**
     * Whether heuristic-based detection is enabled.
     */
    private boolean heuristicDetectionEnabled = true;
    
    /**
     * Whether regex-based detection is enabled.
     */
    private boolean regexDetectionEnabled = true;
    
    /**
     * Whether NER model-based detection is enabled.
     */
    private boolean nerDetectionEnabled = true;
    
    /**
     * Weight for heuristic detection strategy when combining results.
     */
    private double heuristicWeight = 0.7;
    
    /**
     * Weight for regex detection strategy when combining results.
     */
    private double regexWeight = 0.9;
    
    /**
     * Weight for NER model detection strategy when combining results.
     */
    private double nerWeight = 1.0;
    
    /**     * Maximum number of detectors to run in parallel.
     */
    private int maxParallelDetectors = 3;
    
    /**
     * List of enabled detector names.
     * If empty, all detectors are enabled.
     */
    private List<String> enabledDetectors = new ArrayList<>();
    
    /**
     * Timeout in seconds for detection operations.
     */
    private int detectionTimeoutSeconds = 30;
    
    /**
     * Whether to cache detection results.
     */
    private boolean cacheEnabled = true;
    
    /**
     * Maximum size of detection cache.
     */
    private int maxCacheSize = 1000;
    
    /**
     * Cache expiration time in minutes.
     */
    private int cacheExpirationMinutes = 60;
}

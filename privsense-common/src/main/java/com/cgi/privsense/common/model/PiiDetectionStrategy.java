package com.cgi.privsense.common.model;

/**
 * Enum representing different strategies for detecting Personally Identifiable Information (PII).
 * Used to indicate which detection method was used in PII detection results.
 */
public enum PiiDetectionStrategy {
    /**
     * Detection based on column names, data types, and other metadata heuristics
     */
    HEURISTIC,
    
    /**
     * Detection using regular expression pattern matching
     */
    REGEX,
    
    /**
     * Detection using Named Entity Recognition models
     */
    NER_MODEL,
    
    /**
     * Combined approach using multiple strategies
     */
    COMBINED,
    
    /**
     * Other custom detection strategies
     */
    OTHER
}

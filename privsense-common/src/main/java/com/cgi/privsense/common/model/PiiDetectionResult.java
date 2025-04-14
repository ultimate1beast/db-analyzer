package com.cgi.privsense.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of a PII (Personally Identifiable Information) detection operation.
 * Contains information about whether PII was detected, the type of PII, and confidence scores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PiiDetectionResult {

    /**
     * The name of the table that was analyzed.
     */
    private String tableName;
    
    /**
     * The name of the column that was analyzed.
     */
    private String columnName;
    
    /**
     * Whether PII was detected in the column.
     */
    private boolean containsPii;
    
    /**
     * Confidence score for the PII detection (0-1).
     * A higher score indicates higher confidence that the column contains PII.
     */
    private double confidenceScore;
    
    /**
     * The type of PII detected (e.g., NAME, EMAIL, PHONE, etc.).
     */
    private PiiType piiType;
    
    /**
     * The strategy used for PII detection (e.g., HEURISTIC, REGEX, NER_MODEL).
     */
    private PiiDetectionStrategy detectionStrategy;
    
    /**
     * Additional details about the detection, such as matching patterns or examples.
     */
    private String detectionDetails;
}

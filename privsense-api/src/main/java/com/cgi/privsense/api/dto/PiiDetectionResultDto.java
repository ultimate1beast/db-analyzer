package com.cgi.privsense.api.dto;

import com.cgi.privsense.common.model.PiiDetectionStrategy;
import com.cgi.privsense.common.model.PiiType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PII detection results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PiiDetectionResultDto {

    /**
     * Name of the table that was analyzed
     */
    private String tableName;
    
    /**
     * Name of the column that was analyzed
     */
    private String columnName;
    
    /**
     * Whether PII was detected in the column
     */
    private boolean containsPii;
    
    /**
     * Confidence score for the PII detection (0-1)
     */
    private double confidenceScore;
    
    /**
     * Type of PII detected
     */
    private PiiType piiType;
    
    /**
     * Strategy used for detection
     */
    private PiiDetectionStrategy detectionStrategy;
    
    /**
     * Additional details about the detection
     */
    private String detectionDetails;
}

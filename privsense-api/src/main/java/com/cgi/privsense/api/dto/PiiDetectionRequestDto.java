package com.cgi.privsense.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for PII detection requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PiiDetectionRequestDto {

    /**
     * Connection request parameters
     */
    @NotNull(message = "Connection request must not be null")
    private ConnectionRequestDto connectionRequest;

    /**
     * Name of the table to analyze
     */
    @NotBlank(message = "Table name must not be blank")
    private String tableName;

    /**
     * Name of the column to analyze (optional if analyzing entire table)
     */
    private String columnName;

    /**
     * Number of samples to use for detection
     */
    @Min(value = 10, message = "Sample size must be at least 10")
    @Builder.Default
    private int sampleSize = 100;
    
    /**
     * Whether to use heuristic-based detection (column names)
     */
    @Builder.Default
    private boolean enableHeuristicDetection = true;
    
    /**
     * Whether to use regex-based detection (pattern matching)
     */
    @Builder.Default
    private boolean enableRegexDetection = true;
    
    /**
     * Whether to use NER-based detection (named entity recognition)
     */
    @Builder.Default
    private boolean enableNerDetection = true;
    
    /**
     * Minimum confidence score to consider a result as PII (0-1)
     */
    @Builder.Default
    private double minimumConfidence = 0.6;
    
    /**
     * List of specific columns to analyze (if not analyzing all columns)
     */
    private List<String> columns;
}

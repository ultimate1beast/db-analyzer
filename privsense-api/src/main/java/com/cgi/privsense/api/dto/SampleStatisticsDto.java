package com.cgi.privsense.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for statistics about sampled data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleStatisticsDto {

    /**
     * Number of distinct values in the sample
     */
    private int distinctValueCount;

    /**
     * Number of NULL values in the sample
     */
    private int nullCount;

    /**
     * Percentage of NULL values in the sample
     */
    private double nullPercentage;

    /**
     * Minimum value in the sample (for numeric or date columns)
     */
    private String minValue;

    /**
     * Maximum value in the sample (for numeric or date columns)
     */
    private String maxValue;

    /**
     * Distribution of values in the sample
     * Key is the value, value is the count or percentage
     */
    private Map<String, Integer> valueDistribution;

    /**
     * Average length of string values (for string columns)
     */
    private Double averageLength;
}

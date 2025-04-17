package com.cgi.privsense.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents statistics calculated from sampled data of a database column.
 * Provides information about value distribution and uniqueness.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleStatistics {

    /**
     * Number of distinct values found in the sample.
     */
    private int distinctValueCount;
    
    /**
     * Number of NULL values found in the sample.
     */
    private int nullCount;
    
    /**
     * Percentage of NULL values in the sample (0-100).
     */
    private double nullPercentage;
    
    /**
     * The minimum value in the sample (for numeric or date columns).
     * Will be null for non-orderable types.
     */
    private String minValue;
    
    /**
     * The maximum value in the sample (for numeric or date columns).
     * Will be null for non-orderable types.
     */
    private String maxValue;
    
    /**
     * The average length of string values in the sample.
     */
    private double averageLength;
    
    /**
     * Distribution of values in the sample, mapping values to their frequency.
     */
    @Builder.Default
    private Map<String, Integer> valueDistribution = new HashMap<>();
    
    /**
     * Calculates the uniqueness ratio of values in the sample (0-1).
     * A value of 1 means all values are unique, while 0 means all values are the same.
     *
     * @return The uniqueness ratio
     */
    public double getUniquenessRatio() {
        if (valueDistribution.isEmpty()) {
            return 0.0;
        }
        return (double) distinctValueCount / (valueDistribution.values().stream().mapToInt(Integer::intValue).sum());
    }
    
    /**
     * Gets the average length of string values in the sample.
     *
     * @return the average length of values
     */
    public double getAverageLength() {
        return averageLength;
    }
}

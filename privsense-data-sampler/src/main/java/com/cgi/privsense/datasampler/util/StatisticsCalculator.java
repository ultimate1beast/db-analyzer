package com.cgi.privsense.datasampler.util;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.SampleStatistics;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * Utility class for calculating statistics on sampled data.
 * Provides methods for statistical analysis of column samples.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticsCalculator {    /**
     * Calculates statistics for a list of sampled values.
     *
     * @param samples the list of sampled values
     * @param columnMetadata metadata for the column
     * @return the calculated statistics
     */
    public static SampleStatistics calculateStatistics(List<String> samples, ColumnMetadata columnMetadata) {
        log.debug("Calculating statistics for column {}", columnMetadata.getName());
        
        if (samples == null || samples.isEmpty()) {
            return createEmptyStatistics();
        }
          // Calculate basic statistics
        Map<String, Integer> valueDistribution = new HashMap<>();
        int nullCount = 0;
        String minValue = null;
        String maxValue = null;
        boolean initialized = false;
        
        for (String sample : samples) {
            if (sample == null) {
                nullCount++;            } else {
                // Count occurrences for distribution
                valueDistribution.merge(sample, 1, Integer::sum);                // Track min and max values for strings
                if (sample != null && DataTypeUtils.isNumericOrDateType(columnMetadata.getDataType())) {
                    if (!initialized) {
                        minValue = sample;
                        maxValue = sample;
                        initialized = true;
                    } else {
                        if (sample.compareTo(minValue) < 0) {
                            minValue = sample;
                        }
                        if (sample.compareTo(maxValue) > 0) {
                            maxValue = sample;
                        }
                    }
                }
            }
        }
        
        double nullPercentage = samples.isEmpty() ? 0.0 : (double) nullCount / samples.size() * 100;
          // Build and return statistics
        return SampleStatistics.builder()
                .distinctValueCount(valueDistribution.size())
                .nullCount(nullCount)
                .nullPercentage(nullPercentage)
                .valueDistribution(valueDistribution)
                .minValue(minValue)
                .maxValue(maxValue)
                .build();
    }
    
    /**
     * Creates empty statistics for when no samples are available.
     *
     * @return empty statistics
     */
    private static SampleStatistics createEmptyStatistics() {
        return SampleStatistics.builder()
                .distinctValueCount(0)
                .nullCount(0)
                .nullPercentage(0.0)
                .valueDistribution(new HashMap<>())
                .build();
    }
      /**
     * This method has been deprecated as we now use String's native compareTo method.
     * Kept here as a reference in case specialized comparison is needed in the future.
     * 
     * @param value1 The first value to compare
     * @param value2 The second value to compare
     * @return comparison result (-1, 0, or 1)
     */
    @SuppressWarnings("unused")
    private static int compareValues(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return 0;
        }
        if (value1 == null) {
            return -1;
        }
        if (value2 == null) {
            return 1;
        }
        
        // Handle date/time types specially
        if (value1 instanceof Date && value2 instanceof Date) {
            return ((Date) value1).compareTo((Date) value2);
        } else if (value1 instanceof Time && value2 instanceof Time) {
            return ((Time) value1).compareTo((Time) value2);
        } else if (value1 instanceof Timestamp && value2 instanceof Timestamp) {
            return ((Timestamp) value1).compareTo((Timestamp) value2);
        } else if (value1 instanceof Number && value2 instanceof Number) {
            // Convert to BigDecimal for precise number comparison
            BigDecimal bd1 = convertToBigDecimal((Number) value1);
            BigDecimal bd2 = convertToBigDecimal((Number) value2);
            return bd1.compareTo(bd2);        } else if (value1.getClass() == value2.getClass() && value1 instanceof Comparable) {
            // For other comparable types of the same class
            @SuppressWarnings("unchecked")
            Comparable<Object> comparable = (Comparable<Object>) value1;
            return comparable.compareTo(value2);
        } else {
            // Fall back to string comparison
            return value1.toString().compareTo(value2.toString());
        }
    }
    
    /**
     * Converts a Number to BigDecimal for precise comparison.
     *
     * @param number the number to convert
     * @return a BigDecimal representation
     */
    private static BigDecimal convertToBigDecimal(Number number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof Integer || number instanceof Long || number instanceof Short || number instanceof Byte) {
            return new BigDecimal(number.longValue());
        } else {
            return new BigDecimal(number.doubleValue());
        }
    }
}

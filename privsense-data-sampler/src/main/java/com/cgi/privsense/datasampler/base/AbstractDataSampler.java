package com.cgi.privsense.datasampler.base;

import com.cgi.privsense.common.exception.SamplingException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.model.SampleStatistics;
import com.cgi.privsense.common.service.DataSampler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for data samplers that implements common functionality.
 * Concrete samplers should extend this class to inherit common behavior.
 */
public abstract class AbstractDataSampler implements DataSampler {

    /**
     * Template method that defines the standard sampling algorithm.
     * Concrete subclasses will implement specific parts of the algorithm.
     */
    @Override
    public SampleResult sampleColumn(String tableName, ColumnMetadata columnMetadata, int sampleSize) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate input
            validateSamplingParameters(tableName, columnMetadata, sampleSize);
            
            // Perform the actual sampling (to be implemented by subclasses)
            List<String> samples = performSampling(tableName, columnMetadata, sampleSize);
            
            // Calculate statistics from the samples
            SampleStatistics statistics = calculateStatistics(samples);
            
            // Create and return the result
            long executionTime = System.currentTimeMillis() - startTime;
            return SampleResult.builder()
                    .tableName(tableName)
                    .columnName(columnMetadata.getName())
                    .samples(samples)
                    .statistics(statistics)
                    .executionTimeMs(executionTime)
                    .build();
            
        } catch (Exception e) {
            throw new SamplingException("Error sampling column " + 
                    tableName + "." + columnMetadata.getName(), e);
        }
    }
    
    /**
     * Performs the actual sampling operation.
     * This is the strategy-specific part that each subclass must implement.
     *
     * @param tableName the name of the table
     * @param columnMetadata metadata for the column to sample
     * @param sampleSize the number of samples to retrieve
     * @return the list of sampled values
     */
    protected abstract List<String> performSampling(String tableName, ColumnMetadata columnMetadata, int sampleSize);
    
    /**
     * Validates the parameters for the sampling operation.
     *
     * @param tableName the name of the table
     * @param columnMetadata metadata for the column to sample
     * @param sampleSize the number of samples to retrieve
     * @throws IllegalArgumentException if any parameter is invalid
     */
    protected void validateSamplingParameters(String tableName, ColumnMetadata columnMetadata, int sampleSize) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        
        if (columnMetadata == null) {
            throw new IllegalArgumentException("Column metadata cannot be null");
        }
        
        if (columnMetadata.getName() == null || columnMetadata.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be greater than 0");
        }
    }
    
    /**
     * Calculates statistics for the given samples.
     *
     * @param samples the list of samples to analyze
     * @return the calculated statistics
     */
    protected SampleStatistics calculateStatistics(List<String> samples) {
        // Count distinct values and their frequency
        Map<String, Integer> valueDistribution = new HashMap<>();
        int nullCount = 0;
        
        for (String sample : samples) {
            if (sample == null) {
                nullCount++;
            } else {
                valueDistribution.put(sample, valueDistribution.getOrDefault(sample, 0) + 1);
            }
        }
        
        // Calculate null percentage
        double nullPercentage = samples.isEmpty() ? 0 : (nullCount * 100.0) / samples.size();
        
        // Find min and max values (for orderable types)
        String minValue = null;
        String maxValue = null;
        
        if (!valueDistribution.isEmpty()) {
            try {
                List<String> nonNullValues = valueDistribution.keySet().stream().sorted().toList();
                minValue = nonNullValues.get(0);
                maxValue = nonNullValues.get(nonNullValues.size() - 1);
            } catch (Exception e) {
                // If values cannot be compared (e.g., non-orderable types), leave min/max as null
            }
        }
        
        // Build and return the statistics
        return SampleStatistics.builder()
                .distinctValueCount(valueDistribution.size())
                .nullCount(nullCount)
                .nullPercentage(nullPercentage)
                .minValue(minValue)
                .maxValue(maxValue)
                .valueDistribution(valueDistribution)
                .build();
    }
}

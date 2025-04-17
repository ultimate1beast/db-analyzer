package com.cgi.privsense.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a data sampling operation on a database column.
 * Contains the samples retrieved and statistics about the sampling operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleResult {

    /**
     * The name of the table from which samples were taken.
     */
    private String tableName;
    
    /**
     * The name of the column from which samples were taken.
     */
    private String columnName;
    
    /**
     * List of sampled values as strings.
     */
    @Builder.Default
    private List<String> samples = new ArrayList<>();
    
    /**
     * Statistics calculated from the sampled data.
     */
    private SampleStatistics statistics;
    
    /**
     * Total number of rows scanned during the sampling process.
     */
    private long totalRowsScanned;
    
    /**
     * Time taken to execute the sampling operation in milliseconds.
     */
    private long executionTimeMs;
    
    /**
     * The strategy used for sampling the data.
     */
    private String samplingStrategy;
    
    /**
     * Gets the sampling strategy used.
     *
     * @return the sampling strategy
     */
    public String getSamplingStrategy() {
        return samplingStrategy;
    }
}

package com.cgi.privsense.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for data sampling results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleResultDto {

    /**
     * Name of the table the samples were taken from
     */
    private String tableName;

    /**
     * Name of the column the samples were taken from
     */
    private String columnName;

    /**
     * List of sample values
     */
    private List<String> samples;

    /**
     * Statistics about the sampled data
     */
    private SampleStatisticsDto statistics;

    /**
     * Total number of rows that were scanned during sampling
     */
    private long totalRowsScanned;

    /**
     * Execution time in milliseconds
     */
    private long executionTimeMs;

    /**
     * Strategy used for sampling
     */
    private String samplingStrategy;
}

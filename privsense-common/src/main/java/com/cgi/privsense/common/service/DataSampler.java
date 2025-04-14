package com.cgi.privsense.common.service;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.SampleResult;

/**
 * Interface for sampling data from database columns.
 * Implementations should handle different sampling strategies and database types.
 */
public interface DataSampler {

    /**
     * Samples data from a specific column in a table.
     *
     * @param tableName the name of the table
     * @param columnMetadata metadata for the column to sample
     * @param sampleSize the number of samples to retrieve
     * @return the sampling result containing samples and statistics
     * @throws com.cgi.privsense.common.exception.SamplingException if sampling fails
     */
    SampleResult sampleColumn(String tableName, ColumnMetadata columnMetadata, int sampleSize);
}

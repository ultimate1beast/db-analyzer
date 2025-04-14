package com.cgi.privsense.common.service;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.PiiDetectionResult;
import com.cgi.privsense.common.model.PiiDetectionStrategy;
import com.cgi.privsense.common.model.SampleResult;

/**
 * Interface for detecting Personally Identifiable Information (PII) in database columns.
 * Implementations should handle different PII detection strategies.
 */
public interface PiiDetector {

    /**
     * Detects PII in column data based on column metadata and sample data.
     *
     * @param columnMetadata metadata for the column to analyze
     * @param sampleResult sample data from the column
     * @return the PII detection result
     * @throws com.cgi.privsense.common.exception.PiiDetectionException if PII detection fails
     */
    PiiDetectionResult detectPii(ColumnMetadata columnMetadata, SampleResult sampleResult);

    /**
     * Gets the PII detection strategy used by this detector.
     *
     * @return the detection strategy
     */
    PiiDetectionStrategy getDetectionStrategy();
}

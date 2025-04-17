package com.cgi.privsense.piidetector.service;

import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.PiiDetectionResult;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.model.TableMetadata;
import com.cgi.privsense.piidetector.facade.PiiDetectionConfig;
import com.cgi.privsense.piidetector.facade.PiiDetectionFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service for detecting PII in database columns.
 * Provides high-level methods for PII detection in single columns or entire tables.
 */
@Slf4j
@Service("detectorPiiDetectionService")
@RequiredArgsConstructor
@CacheConfig(cacheNames = "pii-detection")
public class PiiDetectionService {

    private final PiiDetectionFacade piiDetectionFacade;
    private final PiiDetectionConfig config;

    /**
     * Detects PII in a single database column.
     * Uses caching to avoid redundant detection calls.
     *
     * @param columnMetadata metadata for the column to analyze
     * @param sampleResult sample data from the column
     * @return PII detection result
     * @throws PiiDetectionException if PII detection fails
     */
    @Cacheable(condition = "#root.target.config.cacheEnabled", 
               key = "#columnMetadata.tableName + '.' + #columnMetadata.name")
    public PiiDetectionResult detectColumnPii(ColumnMetadata columnMetadata, SampleResult sampleResult) {
        log.debug("Detecting PII in column: {}.{}", 
                columnMetadata.getTableName(), columnMetadata.getName());
        
        try {
            return piiDetectionFacade.detectPii(columnMetadata, sampleResult);
        } catch (Exception e) {
            log.error("Error detecting PII in column: {}.{}", 
                    columnMetadata.getTableName(), columnMetadata.getName(), e);
            throw new PiiDetectionException("Failed to detect PII in column", e);
        }
    }

    /**
     * Detects PII in all columns of a table.
     * Processes columns in parallel for better performance.
     *
     * @param tableMetadata metadata for the table
     * @param sampleResults sample data for each column
     * @return list of PII detection results for all columns
     * @throws PiiDetectionException if PII detection fails
     */
    public List<PiiDetectionResult> detectTablePii(
            TableMetadata tableMetadata, 
            List<SampleResult> sampleResults) {
        
        log.debug("Detecting PII in table: {} with {} columns", 
                tableMetadata.getName(), tableMetadata.getColumns().size());
        
        // Create a map of sample results by column name for quick lookup
        var samplesByColumn = sampleResults.stream()
                .collect(Collectors.toMap(
                        SampleResult::getColumnName,
                        sample -> sample,
                        (s1, s2) -> s1)); // Keep first in case of duplicates
        
        try {
            // Process columns in parallel based on configuration
            int parallelism = Math.min(
                    tableMetadata.getColumns().size(), 
                    Runtime.getRuntime().availableProcessors());
            
            ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
            List<CompletableFuture<PiiDetectionResult>> futures = new ArrayList<>();
            
            try {
                for (ColumnMetadata column : tableMetadata.getColumns()) {
                    SampleResult sampleResult = samplesByColumn.get(column.getName());
                    
                    // Skip columns with no samples
                    if (sampleResult == null) {
                        log.warn("No sample data available for column: {}.{}", 
                                tableMetadata.getName(), column.getName());
                        continue;
                    }
                    
                    // Submit detection task
                    CompletableFuture<PiiDetectionResult> future = CompletableFuture.supplyAsync(
                            () -> detectColumnPii(column, sampleResult), 
                            executorService);
                    
                    futures.add(future);
                }
                
                // Wait for all futures to complete
                return futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
                
            } finally {
                executorService.shutdown();
            }
            
        } catch (Exception e) {
            log.error("Error detecting PII in table: {}", tableMetadata.getName(), e);
            throw new PiiDetectionException("Failed to detect PII in table", e);
        }
    }

    /**
     * Detects PII in a batch of columns.
     * Useful for processing selected columns from different tables.
     *
     * @param columns list of columns to analyze
     * @param samples sample data for each column
     * @return list of PII detection results
     * @throws PiiDetectionException if PII detection fails
     */
    public List<PiiDetectionResult> detectBatchPii(
            List<ColumnMetadata> columns, 
            List<SampleResult> samples) {
        
        log.debug("Running batch PII detection for {} columns", columns.size());
        
        // Map sample results by column name for quick lookup
        var samplesByKey = samples.stream()
                .collect(Collectors.toMap(
                        sample -> sample.getTableName() + "." + sample.getColumnName(),
                        sample -> sample,
                        (s1, s2) -> s1)); // Keep first in case of duplicates
        
        try {
            return columns.stream()
                    .map(column -> {
                        SampleResult sample = samplesByKey.get(column.getTableName() + "." + column.getName());
                        if (sample == null) {
                            log.warn("No sample data for column {}.{}", column.getTableName(), column.getName());
                            return null;
                        }
                        
                        try {
                            return detectColumnPii(column, sample);
                        } catch (Exception e) {
                            log.error("Error detecting PII for column {}.{}", 
                                    column.getTableName(), column.getName(), e);
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error in batch PII detection", e);
            throw new PiiDetectionException("Batch PII detection failed", e);
        }
    }
}

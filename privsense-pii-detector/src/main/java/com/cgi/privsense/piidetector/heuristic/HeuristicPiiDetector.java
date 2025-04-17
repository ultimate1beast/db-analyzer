package com.cgi.privsense.piidetector.heuristic;

import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.PiiDetectionResult;
import com.cgi.privsense.common.model.PiiDetectionStrategy;
import com.cgi.privsense.common.model.PiiType;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.service.PiiDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of PiiDetector interface that uses column name heuristics
 * to detect potential PII data.
 */
@Slf4j
@Component
public class HeuristicPiiDetector implements PiiDetector {

    private final ColumnPatternLoader columnPatternLoader;

    @Autowired
    public HeuristicPiiDetector(ColumnPatternLoader columnPatternLoader) {
        this.columnPatternLoader = columnPatternLoader;
    }

    /**
     * Detects PII based on column name patterns and schema-based heuristics.
     *
     * @param columnMetadata metadata for the column to analyze
     * @param sampleResult sample data from the column (ignored in this implementation)
     * @return PII detection result
     */
    @Override
    public PiiDetectionResult detectPii(ColumnMetadata columnMetadata, SampleResult sampleResult) {
        try {
            log.debug("Running heuristic detection for column: {}", columnMetadata.getName());
            
            String columnName = columnMetadata.getName().toLowerCase();
            Map<String, PiiType> patterns = columnPatternLoader.getColumnPatterns();
            
            // Builder for detection result
            PiiDetectionResult.PiiDetectionResultBuilder resultBuilder = PiiDetectionResult.builder()
                    .tableName(columnMetadata.getTableName())
                    .columnName(columnMetadata.getName())
                    .detectionStrategy(PiiDetectionStrategy.HEURISTIC);
            
            // Try to find exact matches first for higher confidence
            Optional<Map.Entry<String, PiiType>> exactMatch = patterns.entrySet().stream()
                    .filter(entry -> columnName.equals(entry.getKey()))
                    .findFirst();
            
            if (exactMatch.isPresent()) {
                log.info("Found exact column name match for potential PII: {}", exactMatch.get().getKey());
                return resultBuilder
                        .containsPii(true)
                        .piiType(exactMatch.get().getValue())
                        .confidenceScore(0.9) // High confidence for exact match
                        .detectionDetails("Exact column name match: " + exactMatch.get().getKey())
                        .build();
            }
            
            // Check for partial matches (contains)
            Optional<Map.Entry<String, PiiType>> partialMatch = patterns.entrySet().stream()
                    .filter(entry -> columnName.contains(entry.getKey()))
                    .findFirst();
            
            if (partialMatch.isPresent()) {
                log.info("Found partial column name match for potential PII: {}", partialMatch.get().getKey());
                return resultBuilder
                        .containsPii(true)
                        .piiType(partialMatch.get().getValue())
                        .confidenceScore(0.7) // Lower confidence for partial match
                        .detectionDetails("Partial column name match: " + partialMatch.get().getKey())
                        .build();
            }
            
            // Check for schema-based heuristics (e.g. columns in tables with certain names)
            if (isSchemaBasedPiiCandidate(columnMetadata)) {
                PiiType inferredType = inferPiiTypeFromSchema(columnMetadata);
                return resultBuilder
                        .containsPii(true)
                        .piiType(inferredType)
                        .confidenceScore(0.5) // Lower confidence for schema-based inference
                        .detectionDetails("Schema-based detection based on table: " + columnMetadata.getTableName())
                        .build();
            }
            
            // No PII detected by heuristic approach
            return resultBuilder
                    .containsPii(false)
                    .confidenceScore(0.1)
                    .build();
            
        } catch (Exception e) {
            log.error("Error during heuristic PII detection for column: {}", columnMetadata.getName(), e);
            throw new PiiDetectionException("Heuristic PII detection failed", e);
        }
    }

    /**
     * Returns the detection strategy used by this detector.
     *
     * @return HEURISTIC detection strategy
     */
    @Override
    public PiiDetectionStrategy getDetectionStrategy() {
        return PiiDetectionStrategy.HEURISTIC;
    }
    
    /**
     * Determines if a column might contain PII based on its schema context.
     */
    private boolean isSchemaBasedPiiCandidate(ColumnMetadata columnMetadata) {
        String tableName = columnMetadata.getTableName().toLowerCase();
        String columnName = columnMetadata.getName().toLowerCase();
        
        // Tables likely to contain PII
        boolean piiRelatedTable = tableName.contains("user") || 
                tableName.contains("customer") ||
                tableName.contains("employee") ||
                tableName.contains("person") ||
                tableName.contains("patient") ||
                tableName.contains("account");
        
        // Columns that might be identity-related but don't have obvious names
        boolean potentialIdColumn = columnName.endsWith("id") || 
                columnName.endsWith("number") ||
                columnName.endsWith("key") ||
                columnName.contains("code");
        
        return piiRelatedTable && potentialIdColumn;
    }
    
    /**
     * Infers PII type based on schema context.
     */
    private PiiType inferPiiTypeFromSchema(ColumnMetadata columnMetadata) {
        String tableName = columnMetadata.getTableName().toLowerCase();
        String columnName = columnMetadata.getName().toLowerCase();
        
        if (tableName.contains("patient") || tableName.contains("medical") || tableName.contains("health")) {
            return PiiType.MEDICAL_RECORD;
        } else if (tableName.contains("payment") || tableName.contains("transaction") || tableName.contains("financial")) {
            return PiiType.FINANCIAL_ACCOUNT;
        } else if (tableName.contains("user") || tableName.contains("account")) {
            if (columnName.contains("id")) {
                return PiiType.ID_NUMBER;
            }
        }
        
        return PiiType.OTHER;
    }
}

package com.cgi.privsense.piidetector.ner;

import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.PiiDetectionResult;
import com.cgi.privsense.common.model.PiiDetectionStrategy;
import com.cgi.privsense.common.model.PiiType;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.service.PiiDetector;
import com.cgi.privsense.piidetector.ner.NerServiceClient.NerAnalysisResult;
import com.cgi.privsense.piidetector.ner.NerServiceClient.NerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Implementation of PiiDetector that uses Named Entity Recognition (NER)
 * to identify potential PII in database column samples.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NerModelPiiDetector implements PiiDetector {

    private final NerServiceClient nerServiceClient;

    /**
     * Detects PII in column data based on NER model analysis.
     *
     * @param columnMetadata metadata for the column to analyze
     * @param sampleResult sample data from the column
     * @return the PII detection result
     * @throws PiiDetectionException if PII detection fails
     */
    @Override
    public PiiDetectionResult detectPii(ColumnMetadata columnMetadata, SampleResult sampleResult) {
        try {
            log.debug("Running NER-based detection for column: {}", columnMetadata.getName());
            
            // Check if there are any samples to analyze
            if (sampleResult == null || CollectionUtils.isEmpty(sampleResult.getSamples())) {
                log.info("No samples available for column: {}", columnMetadata.getName());
                return buildNegativeResult(columnMetadata);
            }
            
            // Filter out null and empty samples
            List<String> validSamples = sampleResult.getSamples().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .collect(Collectors.toList());
            
            if (validSamples.isEmpty()) {
                log.info("No valid samples available for column: {}", columnMetadata.getName());
                return buildNegativeResult(columnMetadata);
            }
            
            // Send samples to NER service for analysis
            NerAnalysisResult analysisResult = nerServiceClient.analyzeTextSamples(validSamples);
            
            // If service is unavailable, return negative result
            if (!analysisResult.isServiceAvailable()) {
                log.warn("NER service unavailable during analysis of column: {}", columnMetadata.getName());
                return buildNegativeResult(columnMetadata);
            }
            
            // If no entities detected, return negative result
            if (analysisResult.getEntitiesByType().isEmpty()) {
                log.info("No entities detected in column: {}", columnMetadata.getName());
                return buildNegativeResult(columnMetadata);
            }
            
            // Calculate confidence scores per PII type
            Map<PiiType, Double> confidenceByType = new HashMap<>();
            
            analysisResult.getEntitiesByType().forEach((piiType, entities) -> {
                // Calculate average confidence for this entity type
                double avgConfidence = entities.stream()
                        .mapToDouble(NerEntity::getConfidence)
                        .average()
                        .orElse(0.0);
                
                // Calculate coverage (percentage of samples with this entity type)
                long uniqueSamplesWithEntity = entities.stream()
                        .map(NerEntity::getText)
                        .distinct()
                        .count();
                double coverage = (double) uniqueSamplesWithEntity / validSamples.size();
                
                // Overall confidence is a combination of entity confidence and coverage
                double overallConfidence = avgConfidence * 0.7 + coverage * 0.3;
                
                confidenceByType.put(piiType, overallConfidence);
            });
            
            // Find PII type with highest confidence
            Entry<PiiType, Double> bestMatch = confidenceByType.entrySet().stream()
                    .max(Comparator.comparing(Entry::getValue))
                    .orElseThrow(() -> new PiiDetectionException("Could not determine best PII type match"));
            
            PiiType bestPiiType = bestMatch.getKey();
            double confidenceScore = bestMatch.getValue();
            
            // Confidence threshold to consider it as PII
            boolean containsPii = confidenceScore >= 0.6;
            
            // Build result with detailed information
            String detectionDetails = buildDetectionDetails(
                    analysisResult.getEntitiesByType().get(bestPiiType),
                    confidenceScore,
                    validSamples.size());
            
            log.info("NER detection for column {}: PII type={}, confidence={}, contains PII={}", 
                    columnMetadata.getName(), bestPiiType, confidenceScore, containsPii);
            
            return PiiDetectionResult.builder()
                    .tableName(columnMetadata.getTableName())
                    .columnName(columnMetadata.getName())
                    .containsPii(containsPii)
                    .confidenceScore(confidenceScore)
                    .piiType(bestPiiType)
                    .detectionStrategy(PiiDetectionStrategy.NER_MODEL)
                    .detectionDetails(detectionDetails)
                    .build();
            
        } catch (Exception e) {
            log.error("Error during NER-based PII detection for column: {}", columnMetadata.getName(), e);
            throw new PiiDetectionException("NER-based PII detection failed", e);
        }
    }

    /**
     * Returns the detection strategy used by this detector.
     *
     * @return NER_MODEL detection strategy
     */
    @Override
    public PiiDetectionStrategy getDetectionStrategy() {
        return PiiDetectionStrategy.NER_MODEL;
    }
    
    /**
     * Builds a negative detection result when no PII is found.
     *
     * @param columnMetadata the column metadata
     * @return negative PII detection result
     */
    private PiiDetectionResult buildNegativeResult(ColumnMetadata columnMetadata) {
        return PiiDetectionResult.builder()
                .tableName(columnMetadata.getTableName())
                .columnName(columnMetadata.getName())
                .containsPii(false)
                .confidenceScore(0.0)
                .detectionStrategy(PiiDetectionStrategy.NER_MODEL)
                .build();
    }
    
    /**
     * Builds detection details string with entity information.
     *
     * @param entities the detected entities
     * @param confidenceScore the overall confidence score
     * @param totalSamples total number of samples analyzed
     * @return formatted detection details
     */
    private String buildDetectionDetails(List<NerEntity> entities, double confidenceScore, int totalSamples) {
        if (entities == null || entities.isEmpty()) {
            return "No entities detected";
        }
        
        // Count unique entity mentions
        long uniqueEntities = entities.stream()
                .map(NerEntity::getText)
                .distinct()
                .count();
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Detected %d unique entities in %d samples (%.1f%% confidence). ", 
                uniqueEntities, totalSamples, confidenceScore * 100));
        
        // Add a few example entities (sanitized)
        List<String> exampleTexts = entities.stream()
                .limit(3)
                .map(entity -> sanitizeForLogging(entity.getText()))
                .collect(Collectors.toList());
        
        if (!exampleTexts.isEmpty()) {
            sb.append("Examples (sanitized): ");
            sb.append(String.join(", ", exampleTexts));
        }
        
        return sb.toString();
    }
    
    /**
     * Sanitizes a potentially sensitive value for logging by masking most characters.
     *
     * @param value the value to sanitize
     * @return sanitized value with most characters masked
     */
    private String sanitizeForLogging(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        
        // Show only first and last character, mask the rest
        return value.substring(0, 1) + 
               "*".repeat(Math.min(value.length() - 2, 10)) + 
               value.substring(value.length() - 1);
    }
}

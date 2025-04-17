package com.cgi.privsense.piidetector.facade;

import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.PiiDetectionResult;
import com.cgi.privsense.common.model.PiiDetectionStrategy;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.service.PiiDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Facade for PII detection that orchestrates multiple detection strategies.
 * Combines and aggregates results from different detectors for more accurate detection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PiiDetectionFacade {

    private final List<PiiDetector> detectors;
    private final PiiDetectionConfig config;
    
    /**
     * Detects PII in a database column using multiple strategies and aggregates the results.
     * Executes detectors in parallel for better performance.
     *
     * @param columnMetadata metadata for the column to analyze
     * @param sampleResult sample data from the column
     * @return aggregated PII detection result with highest confidence
     * @throws PiiDetectionException if PII detection fails
     */
    public PiiDetectionResult detectPii(ColumnMetadata columnMetadata, SampleResult sampleResult) {
        try {
            log.debug("Running orchestrated PII detection for column: {}", columnMetadata.getName());
            
            // Filter detectors based on configuration
            List<PiiDetector> activeDetectors = getActiveDetectors();
            
            if (activeDetectors.isEmpty()) {
                throw new PiiDetectionException("No active PII detectors configured");
            }
            
            // For a single detector, just use it directly without parallelism overhead
            if (activeDetectors.size() == 1) {
                return activeDetectors.get(0).detectPii(columnMetadata, sampleResult);
            }
              // Create a thread pool for parallel execution
            ExecutorService executorService = Executors.newFixedThreadPool(
                Math.min(activeDetectors.size(), config.getMaxParallelDetectors()));
            
            try {
                // Execute all detectors in parallel
                List<CompletableFuture<PiiDetectionResult>> futures = activeDetectors.stream()
                    .map(detector -> CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                log.debug("Executing detector: {}", detector.getClass().getSimpleName());
                                return detector.detectPii(columnMetadata, sampleResult);
                            } catch (Exception e) {
                                log.warn("Detector {} failed: {}", detector.getClass().getSimpleName(), e.getMessage(), e);
                                return null;
                            }
                        }, executorService))
                    .collect(Collectors.toList());
                
                // Combine all future results, filtering out nulls from failed detectors
                List<PiiDetectionResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                if (results.isEmpty()) {
                    throw new PiiDetectionException("All PII detectors failed for column: " + columnMetadata.getName());
                }
                
                // Aggregate results to produce a final result with highest confidence
                return aggregateResults(results);
            } finally {
                executorService.shutdown();
            }
        } catch (Exception e) {
            throw new PiiDetectionException("Failed to detect PII in column " + columnMetadata.getName(), e);
        }
    }
    
    /**
     * Returns active detectors based on configuration.
     */
    private List<PiiDetector> getActiveDetectors() {
        if (config.getEnabledDetectors().isEmpty()) {
            return detectors; // All detectors are active if none are explicitly configured
        }
        
        // Filter detectors based on configuration
        return detectors.stream()
            .filter(detector -> {
                String detectorName = detector.getClass().getSimpleName();
                return config.getEnabledDetectors().contains(detectorName);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Aggregates results from multiple detectors, prioritizing higher confidence results.
     */
    private PiiDetectionResult aggregateResults(List<PiiDetectionResult> results) {
        // If we only have one result, just return it
        if (results.size() == 1) {
            return results.get(0);
        }
        
        // Group results by PII type for easier processing
        Map<String, List<PiiDetectionResult>> resultsByType = results.stream()
            .collect(Collectors.groupingBy(result -> 
                result.getPiiType() != null ? result.getPiiType().name() : "UNKNOWN"));
        
        // Calculate confidence scores for each type
        Map<String, Double> confidenceByType = new HashMap<>();
        resultsByType.forEach((type, typeResults) -> {            double avgConfidence = typeResults.stream()
                .mapToDouble(PiiDetectionResult::getConfidenceScore)
                .average()
                .orElse(0.0);
            // Adjust confidence by the number of detectors that agreed on this type
            double weightedConfidence = avgConfidence * 
                (double)typeResults.size() / (double)results.size();
            confidenceByType.put(type, weightedConfidence);
        });
        
        // Find the type with highest confidence
        String bestType = confidenceByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");
        
        // Get the best result with that type
        List<PiiDetectionResult> bestResults = resultsByType.get(bestType);        PiiDetectionResult bestResult = bestResults.stream()
            .max(Comparator.comparing(PiiDetectionResult::getConfidenceScore))
            .orElse(results.get(0));
          // Log the decision logic
        log.debug("Aggregated {} detection results. Selected type: {}, confidence: {}, strategy: {}",
            results.size(), bestResult.getPiiType(), bestResult.getConfidenceScore(), 
            bestResult.getDetectionStrategy());
        
        return bestResult;
    }
    
    /**
     * Batch process multiple columns for PII detection.
     * 
     * @param columns list of columns with their sample data
     * @return map of column names to their PII detection results
     */
    public Map<String, PiiDetectionResult> detectPiiInBatch(
            List<Map.Entry<ColumnMetadata, SampleResult>> columns) {
        
        Map<String, PiiDetectionResult> results = new HashMap<>();
        
        for (Map.Entry<ColumnMetadata, SampleResult> column : columns) {
            String columnName = column.getKey().getName();
            try {
                PiiDetectionResult result = detectPii(column.getKey(), column.getValue());
                results.put(columnName, result);
            } catch (Exception e) {
                log.error("Failed to detect PII for column {}: {}", columnName, e.getMessage());                results.put(columnName, PiiDetectionResult.builder()
                    .columnName(columnName)
                    .containsPii(false)
                    .detectionStrategy(PiiDetectionStrategy.OTHER)
                    .confidenceScore(0.0)
                    .build());
            }
        }
        
        return results;
    }
}

package com.cgi.privsense.piidetector.regex;

import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.PiiDetectionResult;
import com.cgi.privsense.common.model.PiiDetectionStrategy;
import com.cgi.privsense.common.model.PiiType;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.service.PiiDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the PiiDetector interface that uses regular expressions
 * to detect potential PII data in column samples.
 */
@Slf4j
@Component
public class RegexPiiDetector implements PiiDetector {

    private final PiiPatternLibrary patternLibrary;
    
    @Autowired
    public RegexPiiDetector(PiiPatternLibrary patternLibrary) {
        this.patternLibrary = patternLibrary;
    }

    /**
     * Detects PII in column data using regular expression patterns.
     *
     * @param columnMetadata metadata for the column to analyze
     * @param sampleResult sample data from the column
     * @return PII detection result
     * @throws PiiDetectionException if detection fails
     */
    @Override
    public PiiDetectionResult detectPii(ColumnMetadata columnMetadata, SampleResult sampleResult) {
        try {
            log.debug("Running regex detection for column: {}", columnMetadata.getName());
            
            // Check if there are any samples to analyze
            if (sampleResult == null || CollectionUtils.isEmpty(sampleResult.getSamples())) {
                log.info("No samples available for column: {}", columnMetadata.getName());
                return PiiDetectionResult.builder()
                        .tableName(columnMetadata.getTableName())
                        .columnName(columnMetadata.getName())
                        .containsPii(false)
                        .confidenceScore(0.0)
                        .detectionStrategy(PiiDetectionStrategy.REGEX)
                        .build();
            }
            
            List<String> samples = sampleResult.getSamples();
            
            // Calculate match percentages for each PII type
            Map<PiiType, Double> typeMatchPercentages = new HashMap<>();
            Map<PiiType, String> matchingPatternNames = new HashMap<>();
            Map<PiiType, List<String>> matchedExamples = new HashMap<>();
            
            // Check each PII type's patterns against samples
            for (PiiType piiType : PiiType.values()) {
                Map<String, Pattern> patterns = patternLibrary.getPatternsForType(piiType);
                if (patterns.isEmpty()) {
                    continue; // Skip if no patterns for this type
                }
                
                int totalMatches = 0;
                String bestPatternName = null;
                int bestPatternMatches = 0;
                List<String> examples = new ArrayList<>();
                
                // For each pattern in this PII type
                for (Entry<String, Pattern> patternEntry : patterns.entrySet()) {
                    String patternName = patternEntry.getKey();
                    Pattern pattern = patternEntry.getValue();
                    AtomicInteger matchCount = new AtomicInteger(0);
                    
                    // Check each sample against the pattern
                    samples.stream()
                            .filter(Objects::nonNull)
                            .filter(sample -> !sample.trim().isEmpty())
                            .forEach(sample -> {
                                if (patternLibrary.isValidMatch(pattern, sample, piiType)) {
                                    matchCount.incrementAndGet();
                                    if (examples.size() < 3) { // Keep a few examples
                                        examples.add(sample);
                                    }
                                }
                            });
                    
                    // Update best pattern if this one has more matches
                    int currentMatches = matchCount.get();
                    totalMatches = Math.max(totalMatches, currentMatches);
                    
                    if (currentMatches > bestPatternMatches) {
                        bestPatternMatches = currentMatches;
                        bestPatternName = patternName;
                    }
                }
                
                // Calculate match percentage for this PII type
                double matchPercentage = (double) totalMatches / samples.size();
                if (matchPercentage > 0.0) {
                    typeMatchPercentages.put(piiType, matchPercentage);
                    matchingPatternNames.put(piiType, bestPatternName);
                    matchedExamples.put(piiType, examples);
                }
            }
            
            // If no matches found for any PII type, return negative result
            if (typeMatchPercentages.isEmpty()) {
                log.info("No regex matches found for column: {}", columnMetadata.getName());
                return PiiDetectionResult.builder()
                        .tableName(columnMetadata.getTableName())
                        .columnName(columnMetadata.getName())
                        .containsPii(false)
                        .confidenceScore(0.0)
                        .detectionStrategy(PiiDetectionStrategy.REGEX)
                        .build();
            }
            
            // Get the PII type with highest match percentage
            Entry<PiiType, Double> bestMatch = typeMatchPercentages.entrySet().stream()
                    .max(Comparator.comparing(Entry::getValue))
                    .orElseThrow(() -> new PiiDetectionException("Could not determine best match"));
            
            PiiType bestPiiType = bestMatch.getKey();
            double matchPercentage = bestMatch.getValue();
            double confidenceScore = calculateConfidenceScore(matchPercentage, bestPiiType);
            
            // Confidence threshold to consider it as PII
            boolean containsPii = confidenceScore >= 0.6;
            
            String detectionDetails = buildDetectionDetails(
                    matchPercentage, 
                    matchingPatternNames.get(bestPiiType), 
                    matchedExamples.get(bestPiiType));
            
            log.info("Regex detection for column {}: PII type={}, confidence={}, contains PII={}", 
                    columnMetadata.getName(), bestPiiType, confidenceScore, containsPii);
            
            return PiiDetectionResult.builder()
                    .tableName(columnMetadata.getTableName())
                    .columnName(columnMetadata.getName())
                    .containsPii(containsPii)
                    .confidenceScore(confidenceScore)
                    .piiType(bestPiiType)
                    .detectionStrategy(PiiDetectionStrategy.REGEX)
                    .detectionDetails(detectionDetails)
                    .build();
            
        } catch (Exception e) {
            log.error("Error during regex PII detection for column: {}", columnMetadata.getName(), e);
            throw new PiiDetectionException("Regex PII detection failed", e);
        }
    }

    /**
     * Returns the detection strategy used by this detector.
     *
     * @return REGEX detection strategy
     */
    @Override
    public PiiDetectionStrategy getDetectionStrategy() {
        return PiiDetectionStrategy.REGEX;
    }
    
    /**
     * Calculates a confidence score based on match percentage and PII type.
     * Some PII types require higher match percentages to have high confidence.
     *
     * @param matchPercentage percentage of samples that matched the pattern
     * @param piiType the detected PII type
     * @return confidence score between 0 and 1
     */
    private double calculateConfidenceScore(double matchPercentage, PiiType piiType) {
        // Base confidence from match percentage
        double baseConfidence = matchPercentage;
        
        // Adjust based on PII type - some types need stricter validation
        switch (piiType) {
            case EMAIL:
            case CREDIT_CARD:
            case SSN:
                // High-confidence PII types get a boost if match percentage is high
                return matchPercentage >= 0.8 ? Math.min(1.0, baseConfidence * 1.1) : baseConfidence;
                
            case NAME:
            case ADDRESS:
                // These types are more prone to false positives, so reduce confidence slightly
                return baseConfidence * 0.9;
                
            default:
                return baseConfidence;
        }
    }
    
    /**
     * Builds a detailed description of the detection result.
     *
     * @param matchPercentage percentage of samples that matched
     * @param patternName name of the matching pattern
     * @param examples example values that matched (sanitized)
     * @return formatted detection details
     */
    private String buildDetectionDetails(double matchPercentage, String patternName, List<String> examples) {
        StringBuilder details = new StringBuilder();
        details.append(String.format("Matched %.1f%% of samples using pattern '%s'", 
                matchPercentage * 100, patternName));
        
        if (!examples.isEmpty()) {
            details.append(". Sample matches (sanitized): ");
            details.append(examples.stream()
                    .map(this::sanitizeForLogging)
                    .collect(Collectors.joining(", ")));
        }
        
        return details.toString();
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
        
        // Show only first and last 2 characters, mask the rest
        return value.substring(0, 2) + 
               "*".repeat(Math.min(value.length() - 4, 10)) + 
               value.substring(value.length() - 2);
    }
}

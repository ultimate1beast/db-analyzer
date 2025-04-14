package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the PiiDetectionResult model.
 */
public class PiiDetectionResultTest {

    @Test
    void testPiiDetectionResultBuilder() {
        // Create PII detection result using builder
        PiiDetectionResult result = PiiDetectionResult.builder()
                .tableName("customers")
                .columnName("email")
                .containsPii(true)
                .confidenceScore(0.95)
                .piiType(PiiType.EMAIL)
                .detectionStrategy(PiiDetectionStrategy.REGEX)
                .detectionDetails("Matched email pattern with 95% confidence")
                .build();
        
        // Verify values
        assertEquals("customers", result.getTableName(), "Table name should match");
        assertEquals("email", result.getColumnName(), "Column name should match");
        assertTrue(result.isContainsPii(), "Should contain PII");
        assertEquals(0.95, result.getConfidenceScore(), 0.001, "Confidence score should match");
        assertEquals(PiiType.EMAIL, result.getPiiType(), "PII type should match");
        assertEquals(PiiDetectionStrategy.REGEX, result.getDetectionStrategy(), "Detection strategy should match");
        assertEquals("Matched email pattern with 95% confidence", result.getDetectionDetails(), 
                "Detection details should match");
    }
    
    @Test
    void testPiiDetectionResultEquality() {
        // Create two identical PII detection result objects
        PiiDetectionResult result1 = PiiDetectionResult.builder()
                .tableName("customers")
                .columnName("email")
                .containsPii(true)
                .piiType(PiiType.EMAIL)
                .build();
        
        PiiDetectionResult result2 = PiiDetectionResult.builder()
                .tableName("customers")
                .columnName("email")
                .containsPii(true)
                .piiType(PiiType.EMAIL)
                .build();
        
        // Verify equality
        assertEquals(result1, result2, "Equal PII detection results should be equal");
        assertEquals(result1.hashCode(), result2.hashCode(), "Hash codes should match for equal objects");
        
        // Modify one object
        result2.setPiiType(PiiType.NAME);
        
        // Verify inequality
        assertNotEquals(result1, result2, "Different PII detection results should not be equal");
    }
    
    @Test
    void testNoArgsConstructor() {
        // Create PII detection result using no-args constructor
        PiiDetectionResult result = new PiiDetectionResult();
        
        // Set properties
        result.setTableName("customers");
        result.setColumnName("email");
        result.setContainsPii(true);
        
        // Verify values
        assertEquals("customers", result.getTableName(), "Table name should match");
        assertEquals("email", result.getColumnName(), "Column name should match");
        assertTrue(result.isContainsPii(), "Should contain PII");
    }
}

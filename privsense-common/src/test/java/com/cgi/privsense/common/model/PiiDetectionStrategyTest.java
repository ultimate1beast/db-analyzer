package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the PiiDetectionStrategy enum.
 */
public class PiiDetectionStrategyTest {

    @Test
    void testPiiDetectionStrategyValues() {
        // Verify that the enum contains the expected values
        assertEquals(5, PiiDetectionStrategy.values().length, "PiiDetectionStrategy should have 5 values");
        
        // Check all enum values
        assertEquals("HEURISTIC", PiiDetectionStrategy.HEURISTIC.name(), "HEURISTIC enum constant should exist");
        assertEquals("REGEX", PiiDetectionStrategy.REGEX.name(), "REGEX enum constant should exist");
        assertEquals("NER_MODEL", PiiDetectionStrategy.NER_MODEL.name(), "NER_MODEL enum constant should exist");
        assertEquals("COMBINED", PiiDetectionStrategy.COMBINED.name(), "COMBINED enum constant should exist");
        assertEquals("OTHER", PiiDetectionStrategy.OTHER.name(), "OTHER enum constant should exist");
    }
    
    @Test
    void testPiiDetectionStrategyOrdinals() {
        // Verify ordinal values are consistent
        // This helps detect if someone changes the order of enum values, which could break serialization
        assertEquals(0, PiiDetectionStrategy.HEURISTIC.ordinal());
        assertEquals(1, PiiDetectionStrategy.REGEX.ordinal());
        assertEquals(2, PiiDetectionStrategy.NER_MODEL.ordinal());
        assertEquals(3, PiiDetectionStrategy.COMBINED.ordinal());
        assertEquals(4, PiiDetectionStrategy.OTHER.ordinal());
    }
    
    @Test
    void testPiiDetectionStrategyValueOf() {
        // Test valueOf method for enum constants
        assertEquals(PiiDetectionStrategy.HEURISTIC, PiiDetectionStrategy.valueOf("HEURISTIC"));
        assertEquals(PiiDetectionStrategy.REGEX, PiiDetectionStrategy.valueOf("REGEX"));
        assertEquals(PiiDetectionStrategy.NER_MODEL, PiiDetectionStrategy.valueOf("NER_MODEL"));
        
        // Test for exception with invalid name
        assertThrows(IllegalArgumentException.class, () -> PiiDetectionStrategy.valueOf("INVALID_STRATEGY"));
    }
}

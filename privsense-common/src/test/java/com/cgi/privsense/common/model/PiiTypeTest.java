package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the PiiType enum.
 */
public class PiiTypeTest {

    @Test
    void testPiiTypeValues() {
        // Verify that the enum contains the expected values
        assertEquals(15, PiiType.values().length, "PiiType should have 15 values");
        
        // Check a few key enum values
        assertEquals("NAME", PiiType.NAME.name(), "NAME enum constant should exist");
        assertEquals("EMAIL", PiiType.EMAIL.name(), "EMAIL enum constant should exist");
        assertEquals("PHONE", PiiType.PHONE.name(), "PHONE enum constant should exist");
        assertEquals("ADDRESS", PiiType.ADDRESS.name(), "ADDRESS enum constant should exist");
        assertEquals("SSN", PiiType.SSN.name(), "SSN enum constant should exist");
        assertEquals("CREDIT_CARD", PiiType.CREDIT_CARD.name(), "CREDIT_CARD enum constant should exist");
    }
    
    @Test
    void testPiiTypeOrdinals() {
        // Verify ordinal values are consistent
        // This helps detect if someone changes the order of enum values, which could break serialization
        assertEquals(0, PiiType.NAME.ordinal());
        assertEquals(1, PiiType.EMAIL.ordinal());
        assertEquals(2, PiiType.PHONE.ordinal());
    }
    
    @Test
    void testPiiTypeValueOf() {
        // Test valueOf method for enum constants
        assertEquals(PiiType.NAME, PiiType.valueOf("NAME"));
        assertEquals(PiiType.EMAIL, PiiType.valueOf("EMAIL"));
        assertEquals(PiiType.PHONE, PiiType.valueOf("PHONE"));
        
        // Test for exception with invalid name
        assertThrows(IllegalArgumentException.class, () -> PiiType.valueOf("INVALID_TYPE"));
    }
}

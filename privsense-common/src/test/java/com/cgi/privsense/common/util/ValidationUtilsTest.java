package com.cgi.privsense.common.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ValidationUtils utility methods.
 */
public class ValidationUtilsTest {

    @Test
    void testNotNull() {
        // Should not throw exception for non-null object
        ValidationUtils.notNull("test", "Object should not be null");
        
        // Should throw exception for null object
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.notNull(null, "Object should not be null");
        });
        
        assertEquals("Object should not be null", exception.getMessage(), 
                "Exception message should match the provided message");
    }
    
    @Test
    void testNotEmpty() {
        // Should not throw exception for non-empty string
        ValidationUtils.notEmpty("test", "String should not be empty");
        
        // Should throw exception for null string
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.notEmpty(null, "String should not be empty");
        }, "Should throw exception for null string");
        
        // Should throw exception for empty string
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.notEmpty("", "String should not be empty");
        }, "Should throw exception for empty string");
        
        // Should throw exception for whitespace-only string
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.notEmpty("   ", "String should not be empty");
        }, "Should throw exception for whitespace-only string");
    }
    
    @Test
    void testContainsKey() {
        // Create test map
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        // Should not throw exception when key exists
        ValidationUtils.containsKey(map, "key1", "Map should contain the key");
        
        // Should throw exception when key does not exist
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.containsKey(map, "key3", "Map should contain the key");
        }, "Should throw exception for missing key");
        
        // Should throw exception for null map
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.containsKey(null, "key", "Map cannot be null");
        }, "Should throw exception for null map");
    }
    
    @Test
    void testPositive() {
        // Should not throw exception for positive number
        ValidationUtils.positive(10, "Number should be positive");
        
        // Should throw exception for zero
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.positive(0, "Number should be positive");
        }, "Should throw exception for zero");
        
        // Should throw exception for negative number
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.positive(-5, "Number should be positive");
        }, "Should throw exception for negative number");
    }
}

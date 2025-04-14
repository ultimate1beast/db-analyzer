package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the SampleResult model.
 */
public class SampleResultTest {

    @Test
    void testSampleResultBuilder() {
        // Create sample statistics
        SampleStatistics statistics = SampleStatistics.builder()
                .distinctValueCount(3)
                .nullCount(1)
                .nullPercentage(10.0)
                .build();
        
        // Create list of sample values
        List<String> samples = Arrays.asList("value1", "value2", "value3", null);
        
        // Create sample result using builder
        SampleResult result = SampleResult.builder()
                .tableName("employees")
                .columnName("email")
                .samples(samples)
                .statistics(statistics)
                .totalRowsScanned(100)
                .executionTimeMs(150)
                .build();
        
        // Verify values
        assertEquals("employees", result.getTableName(), "Table name should match");
        assertEquals("email", result.getColumnName(), "Column name should match");
        assertEquals(4, result.getSamples().size(), "Sample count should match");
        assertEquals(statistics, result.getStatistics(), "Statistics should match");
        assertEquals(100, result.getTotalRowsScanned(), "Total rows scanned should match");
        assertEquals(150, result.getExecutionTimeMs(), "Execution time should match");
    }
    
    @Test
    void testSampleResultDefaultValues() {
        // Create sample result with default values
        SampleResult result = SampleResult.builder()
                .tableName("employees")
                .columnName("email")
                .build();
        
        // Verify default values
        assertNotNull(result.getSamples(), "Samples list should not be null by default");
        assertEquals(0, result.getSamples().size(), "Samples list should be empty by default");
    }
    
    @Test
    void testSampleResultEquality() {
        // Create two identical sample results
        SampleResult result1 = SampleResult.builder()
                .tableName("employees")
                .columnName("email")
                .totalRowsScanned(100)
                .build();
        
        SampleResult result2 = SampleResult.builder()
                .tableName("employees")
                .columnName("email")
                .totalRowsScanned(100)
                .build();
        
        // Verify equality
        assertEquals(result1, result2, "Equal sample results should be equal");
        assertEquals(result1.hashCode(), result2.hashCode(), "Hash codes should match for equal objects");
        
        // Modify one object
        result2.setTotalRowsScanned(200);
        
        // Verify inequality
        assertNotEquals(result1, result2, "Different sample results should not be equal");
    }
    
    @Test
    void testNoArgsConstructor() {
        // Create sample result using no-args constructor
        SampleResult result = new SampleResult();
        
        // Set properties
        result.setTableName("employees");
        result.setColumnName("email");
        
        // Verify values
        assertEquals("employees", result.getTableName(), "Table name should match");
        assertEquals("email", result.getColumnName(), "Column name should match");
        assertNotNull(result.getSamples(), "Samples list should not be null");
    }
}

package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the SampleStatistics model.
 */
public class SampleStatisticsTest {

    @Test
    void testSampleStatisticsBuilder() {
        // Create value distribution map
        Map<String, Integer> valueDistribution = new HashMap<>();
        valueDistribution.put("male", 60);
        valueDistribution.put("female", 40);
        
        // Create sample statistics using builder
        SampleStatistics statistics = SampleStatistics.builder()
                .distinctValueCount(2)
                .nullCount(5)
                .nullPercentage(5.0)
                .minValue("female")
                .maxValue("male")
                .valueDistribution(valueDistribution)
                .build();
        
        // Verify values
        assertEquals(2, statistics.getDistinctValueCount(), "Distinct value count should match");
        assertEquals(5, statistics.getNullCount(), "Null count should match");
        assertEquals(5.0, statistics.getNullPercentage(), "Null percentage should match");
        assertEquals("female", statistics.getMinValue(), "Min value should match");
        assertEquals("male", statistics.getMaxValue(), "Max value should match");
        assertEquals(valueDistribution, statistics.getValueDistribution(), "Value distribution should match");
    }
    
    @Test
    void testSampleStatisticsDefaultValues() {
        // Create sample statistics with default values
        SampleStatistics statistics = SampleStatistics.builder().build();
        
        // Verify default values
        assertNotNull(statistics.getValueDistribution(), "Value distribution should not be null by default");
        assertEquals(0, statistics.getValueDistribution().size(), "Value distribution should be empty by default");
    }
    
    @Test
    void testGetUniquenessRatio() {
        // Create value distribution map
        Map<String, Integer> valueDistribution = new HashMap<>();
        valueDistribution.put("value1", 10);
        valueDistribution.put("value2", 20);
        valueDistribution.put("value3", 30);
        valueDistribution.put("value4", 40);
        
        // Create sample statistics
        SampleStatistics statistics = SampleStatistics.builder()
                .distinctValueCount(4)
                .valueDistribution(valueDistribution)
                .build();
        
        // Calculate expected uniqueness ratio: distinctValueCount / total values
        double expectedRatio = 4.0 / (10 + 20 + 30 + 40);
        
        // Verify uniqueness ratio
        assertEquals(expectedRatio, statistics.getUniquenessRatio(), 0.001, 
                "Uniqueness ratio should match expected value");
    }
    
    @Test
    void testGetUniquenessRatioWithEmptyDistribution() {
        // Create sample statistics with empty value distribution
        SampleStatistics statistics = SampleStatistics.builder()
                .distinctValueCount(0)
                .build();
        
        // Verify uniqueness ratio with empty distribution is 0
        assertEquals(0.0, statistics.getUniquenessRatio(), 
                "Uniqueness ratio should be 0 for empty value distribution");
    }
    
    @Test
    void testSampleStatisticsEquality() {
        // Create two identical sample statistics objects
        SampleStatistics statistics1 = SampleStatistics.builder()
                .distinctValueCount(2)
                .nullCount(5)
                .build();
        
        SampleStatistics statistics2 = SampleStatistics.builder()
                .distinctValueCount(2)
                .nullCount(5)
                .build();
        
        // Verify equality
        assertEquals(statistics1, statistics2, "Equal sample statistics should be equal");
        assertEquals(statistics1.hashCode(), statistics2.hashCode(), "Hash codes should match for equal objects");
        
        // Modify one object
        statistics2.setDistinctValueCount(3);
        
        // Verify inequality
        assertNotEquals(statistics1, statistics2, "Different sample statistics should not be equal");
    }
}

package com.cgi.privsense.common.service;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.SampleResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for the DataSampler interface.
 * Uses mockito to test contract behavior.
 */
public class DataSamplerTest {

    @Test
    void testDataSamplerContract() {
        // Create a mock implementation of the interface
        DataSampler sampler = Mockito.mock(DataSampler.class);
        
        // Create test data
        String tableName = "customers";
        ColumnMetadata columnMetadata = ColumnMetadata.builder()
                .name("email")
                .dataType("VARCHAR")
                .build();
        int sampleSize = 100;
        
        // Create a mock result
        SampleResult mockResult = SampleResult.builder()
                .tableName(tableName)
                .columnName("email")
                .build();
        
        // Configure mock behavior
        when(sampler.sampleColumn(anyString(), any(ColumnMetadata.class), anyInt()))
                .thenReturn(mockResult);
        
        // Execute the method under test
        SampleResult result = sampler.sampleColumn(tableName, columnMetadata, sampleSize);
        
        // Verify behavior
        assertNotNull(result, "Sample result should not be null");
        assertEquals(tableName, result.getTableName(), "Table name should match");
        assertEquals("email", result.getColumnName(), "Column name should match");
        
        // Verify the method was called with expected parameters
        verify(sampler).sampleColumn(tableName, columnMetadata, sampleSize);
    }
}

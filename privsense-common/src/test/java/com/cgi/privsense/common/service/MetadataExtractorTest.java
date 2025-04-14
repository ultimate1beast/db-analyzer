package com.cgi.privsense.common.service;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.TableMetadata;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for the MetadataExtractor interface.
 * Uses mockito to test contract behavior.
 */
public class MetadataExtractorTest {

    @Test
    void testExtractAllTablesMetadataContract() {
        // Create a mock implementation of the interface
        MetadataExtractor extractor = Mockito.mock(MetadataExtractor.class);
        
        // Create mock table metadata
        TableMetadata table1 = TableMetadata.builder()
                .name("users")
                .schema("public")
                .build();
        
        TableMetadata table2 = TableMetadata.builder()
                .name("products")
                .schema("public")
                .build();
        
        List<TableMetadata> expectedTables = Arrays.asList(table1, table2);
        
        // Configure mock behavior
        when(extractor.extractAllTablesMetadata()).thenReturn(expectedTables);
        
        // Execute the method under test
        List<TableMetadata> tables = extractor.extractAllTablesMetadata();
        
        // Verify behavior
        assertNotNull(tables, "Tables list should not be null");
        assertEquals(2, tables.size(), "Should return 2 tables");
        assertEquals("users", tables.get(0).getName(), "First table name should be 'users'");
        assertEquals("products", tables.get(1).getName(), "Second table name should be 'products'");
        
        // Verify the method was called
        verify(extractor).extractAllTablesMetadata();
    }
    
    @Test
    void testExtractTableMetadataContract() {
        // Create a mock implementation of the interface
        MetadataExtractor extractor = Mockito.mock(MetadataExtractor.class);
        
        // Create mock column metadata
        ColumnMetadata idColumn = ColumnMetadata.builder()
                .name("id")
                .dataType("INTEGER")
                .isPrimaryKey(true)
                .build();
        
        ColumnMetadata nameColumn = ColumnMetadata.builder()
                .name("name")
                .dataType("VARCHAR")
                .build();
        
        // Create mock table metadata
        TableMetadata expectedTable = TableMetadata.builder()
                .name("users")
                .schema("public")
                .columns(Arrays.asList(idColumn, nameColumn))
                .primaryKeys(Arrays.asList("id"))
                .build();
        
        // Configure mock behavior
        when(extractor.extractTableMetadata("users")).thenReturn(expectedTable);
        
        // Execute the method under test
        TableMetadata table = extractor.extractTableMetadata("users");
        
        // Verify behavior
        assertNotNull(table, "Table metadata should not be null");
        assertEquals("users", table.getName(), "Table name should be 'users'");
        assertEquals(2, table.getColumns().size(), "Table should have 2 columns");
        assertTrue(table.getColumns().get(0).isPrimaryKey(), "First column should be a primary key");
        
        // Verify the method was called with the correct parameters
        verify(extractor).extractTableMetadata("users");
    }
    
    @Test
    void testExtractTablesMetadataContract() {
        // Create a mock implementation of the interface
        MetadataExtractor extractor = Mockito.mock(MetadataExtractor.class);
        
        // Create mock table metadata
        TableMetadata table1 = TableMetadata.builder()
                .name("user_details")
                .schema("public")
                .build();
        
        TableMetadata table2 = TableMetadata.builder()
                .name("user_preferences")
                .schema("public")
                .build();
        
        List<TableMetadata> expectedTables = Arrays.asList(table1, table2);
        
        // Configure mock behavior
        when(extractor.extractTablesMetadata("user_%")).thenReturn(expectedTables);
        
        // Execute the method under test
        List<TableMetadata> tables = extractor.extractTablesMetadata("user_%");
        
        // Verify behavior
        assertNotNull(tables, "Tables list should not be null");
        assertEquals(2, tables.size(), "Should return 2 tables");
        assertTrue(tables.get(0).getName().startsWith("user_"), "Table should match pattern");
        assertTrue(tables.get(1).getName().startsWith("user_"), "Table should match pattern");
        
        // Verify the method was called with the correct parameters
        verify(extractor).extractTablesMetadata("user_%");
    }
}

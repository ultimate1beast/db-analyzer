package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the TableMetadata model.
 */
public class TableMetadataTest {

    @Test
    void testTableMetadataBuilder() {
        // Create table metadata using builder
        TableMetadata metadata = TableMetadata.builder()
                .name("users")
                .schema("public")
                .rowCount(1000L)
                .sizeInBytes(50000L)
                .build();
        
        // Verify values
        assertEquals("users", metadata.getName(), "Table name should match");
        assertEquals("public", metadata.getSchema(), "Schema should match");
        assertEquals(1000L, metadata.getRowCount(), "Row count should match");
        assertEquals(50000L, metadata.getSizeInBytes(), "Size in bytes should match");
        assertNotNull(metadata.getColumns(), "Columns list should not be null");
        assertNotNull(metadata.getPrimaryKeys(), "Primary keys list should not be null");
        assertNotNull(metadata.getForeignKeys(), "Foreign keys list should not be null");
    }
    
    @Test
    void testTableMetadataWithColumns() {
        // Create column metadata
        ColumnMetadata idColumn = ColumnMetadata.builder()
                .name("id")
                .dataType("INTEGER")
                .ordinalPosition(0)
                .isPrimaryKey(true)
                .nullable(false)
                .build();
        
        ColumnMetadata nameColumn = ColumnMetadata.builder()
                .name("name")
                .dataType("VARCHAR")
                .characterMaxLength(100)
                .ordinalPosition(1)
                .nullable(false)
                .build();
        
        // Create table metadata with columns
        TableMetadata metadata = TableMetadata.builder()
                .name("users")
                .schema("public")
                .columns(Arrays.asList(idColumn, nameColumn))
                .primaryKeys(Arrays.asList("id"))
                .build();
        
        // Verify columns
        assertEquals(2, metadata.getColumns().size(), "Should have two columns");
        assertEquals("id", metadata.getColumns().get(0).getName(), "First column should be id");
        assertEquals("name", metadata.getColumns().get(1).getName(), "Second column should be name");
        assertEquals(1, metadata.getPrimaryKeys().size(), "Should have one primary key");
        assertEquals("id", metadata.getPrimaryKeys().get(0), "Primary key should be id");
    }
    
    @Test
    void testTableMetadataWithForeignKeys() {
        // Create foreign key metadata
        ForeignKeyMetadata fkMetadata = ForeignKeyMetadata.builder()
                .constraintName("fk_user_address")
                .columnName("address_id")
                .referencedTable("addresses")
                .referencedColumn("id")
                .tableName("users")
                .schemaName("public")
                .referencedTableSchema("public")
                .build();
        
        // Create table metadata with foreign key
        TableMetadata metadata = TableMetadata.builder()
                .name("users")
                .schema("public")
                .foreignKeys(Arrays.asList(fkMetadata))
                .build();
        
        // Verify foreign keys
        assertEquals(1, metadata.getForeignKeys().size(), "Should have one foreign key");
        assertEquals("fk_user_address", metadata.getForeignKeys().get(0).getConstraintName(), 
                "Foreign key constraint name should match");
        assertEquals("addresses", metadata.getForeignKeys().get(0).getReferencedTable(), 
                "Referenced table should match");
    }
    
    @Test
    void testTableMetadataEquality() {
        // Create two identical table metadata objects
        TableMetadata metadata1 = TableMetadata.builder()
                .name("users")
                .schema("public")
                .build();
        
        TableMetadata metadata2 = TableMetadata.builder()
                .name("users")
                .schema("public")
                .build();
        
        // Verify equality
        assertEquals(metadata1, metadata2, "Equal table metadata objects should be equal");
        assertEquals(metadata1.hashCode(), metadata2.hashCode(), "Hash codes should match for equal objects");
        
        // Modify one object
        metadata2.setName("customers");
        
        // Verify inequality
        assertNotEquals(metadata1, metadata2, "Different table metadata objects should not be equal");
    }
}

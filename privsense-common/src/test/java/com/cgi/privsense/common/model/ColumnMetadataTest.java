package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the ColumnMetadata model.
 */
public class ColumnMetadataTest {

    @Test
    void testColumnMetadataBuilder() {
        // Create column metadata using builder
        ColumnMetadata metadata = ColumnMetadata.builder()
                .name("email")
                .dataType("VARCHAR")
                .ordinalPosition(2)
                .nullable(false)
                .characterMaxLength(255)
                .isPrimaryKey(false)
                .isAutoIncrement(false)
                .defaultValue("''")
                .tableName("users")
                .schemaName("public")
                .build();
        
        // Verify values
        assertEquals("email", metadata.getName(), "Column name should match");
        assertEquals("VARCHAR", metadata.getDataType(), "Data type should match");
        assertEquals(2, metadata.getOrdinalPosition(), "Ordinal position should match");
        assertFalse(metadata.isNullable(), "Column should not be nullable");
        assertEquals(255, metadata.getCharacterMaxLength(), "Character max length should match");
        assertFalse(metadata.isPrimaryKey(), "Column should not be primary key");
        assertFalse(metadata.isAutoIncrement(), "Column should not be auto increment");
        assertEquals("''", metadata.getDefaultValue(), "Default value should match");
        assertEquals("users", metadata.getTableName(), "Table name should match");
        assertEquals("public", metadata.getSchemaName(), "Schema name should match");
    }
    
    @Test
    void testColumnMetadataPrimaryKeyBuilder() {
        // Create primary key column metadata
        ColumnMetadata metadata = ColumnMetadata.builder()
                .name("id")
                .dataType("INTEGER")
                .ordinalPosition(0)
                .nullable(false)
                .isPrimaryKey(true)
                .isAutoIncrement(true)
                .tableName("users")
                .schemaName("public")
                .build();
        
        // Verify primary key properties
        assertTrue(metadata.isPrimaryKey(), "Column should be primary key");
        assertTrue(metadata.isAutoIncrement(), "Column should be auto increment");
    }
    
    @Test
    void testColumnMetadataEquality() {
        // Create two identical column metadata objects
        ColumnMetadata metadata1 = ColumnMetadata.builder()
                .name("email")
                .dataType("VARCHAR")
                .ordinalPosition(2)
                .tableName("users")
                .build();
        
        ColumnMetadata metadata2 = ColumnMetadata.builder()
                .name("email")
                .dataType("VARCHAR")
                .ordinalPosition(2)
                .tableName("users")
                .build();
        
        // Verify equality
        assertEquals(metadata1, metadata2, "Equal column metadata objects should be equal");
        assertEquals(metadata1.hashCode(), metadata2.hashCode(), "Hash codes should match for equal objects");
        
        // Modify one object
        metadata2.setDataType("TEXT");
        
        // Verify inequality
        assertNotEquals(metadata1, metadata2, "Different column metadata objects should not be equal");
    }
    
    @Test
    void testNoArgsConstructor() {
        // Create column metadata using no-args constructor
        ColumnMetadata metadata = new ColumnMetadata();
        
        // Set properties
        metadata.setName("email");
        metadata.setDataType("VARCHAR");
        metadata.setOrdinalPosition(2);
        
        // Verify values
        assertEquals("email", metadata.getName(), "Column name should match");
        assertEquals("VARCHAR", metadata.getDataType(), "Data type should match");
        assertEquals(2, metadata.getOrdinalPosition(), "Ordinal position should match");
    }
}

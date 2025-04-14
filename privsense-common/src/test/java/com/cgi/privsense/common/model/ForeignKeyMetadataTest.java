package com.cgi.privsense.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the ForeignKeyMetadata model.
 */
public class ForeignKeyMetadataTest {

    @Test
    void testForeignKeyMetadataBuilder() {
        // Create foreign key metadata using builder
        ForeignKeyMetadata metadata = ForeignKeyMetadata.builder()
                .constraintName("fk_order_customer")
                .columnName("customer_id")
                .referencedTable("customers")
                .referencedColumn("id")
                .tableName("orders")
                .schemaName("sales")
                .referencedTableSchema("public")
                .build();
        
        // Verify values
        assertEquals("fk_order_customer", metadata.getConstraintName(), "Constraint name should match");
        assertEquals("customer_id", metadata.getColumnName(), "Column name should match");
        assertEquals("customers", metadata.getReferencedTable(), "Referenced table should match");
        assertEquals("id", metadata.getReferencedColumn(), "Referenced column should match");
        assertEquals("orders", metadata.getTableName(), "Table name should match");
        assertEquals("sales", metadata.getSchemaName(), "Schema name should match");
        assertEquals("public", metadata.getReferencedTableSchema(), "Referenced table schema should match");
    }
    
    @Test
    void testForeignKeyMetadataEquality() {
        // Create two identical foreign key metadata objects
        ForeignKeyMetadata metadata1 = ForeignKeyMetadata.builder()
                .constraintName("fk_order_customer")
                .columnName("customer_id")
                .referencedTable("customers")
                .referencedColumn("id")
                .build();
        
        ForeignKeyMetadata metadata2 = ForeignKeyMetadata.builder()
                .constraintName("fk_order_customer")
                .columnName("customer_id")
                .referencedTable("customers")
                .referencedColumn("id")
                .build();
        
        // Verify equality
        assertEquals(metadata1, metadata2, "Equal foreign key metadata objects should be equal");
        assertEquals(metadata1.hashCode(), metadata2.hashCode(), "Hash codes should match for equal objects");
        
        // Modify one object
        metadata2.setConstraintName("fk_order_client");
        
        // Verify inequality
        assertNotEquals(metadata1, metadata2, "Different foreign key metadata objects should not be equal");
    }
    
    @Test
    void testNoArgsConstructor() {
        // Create foreign key metadata using no-args constructor
        ForeignKeyMetadata metadata = new ForeignKeyMetadata();
        
        // Set properties
        metadata.setConstraintName("fk_order_customer");
        metadata.setColumnName("customer_id");
        metadata.setReferencedTable("customers");
        
        // Verify values
        assertEquals("fk_order_customer", metadata.getConstraintName(), "Constraint name should match");
        assertEquals("customer_id", metadata.getColumnName(), "Column name should match");
        assertEquals("customers", metadata.getReferencedTable(), "Referenced table should match");
    }
}

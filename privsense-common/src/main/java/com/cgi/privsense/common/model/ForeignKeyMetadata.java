package com.cgi.privsense.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents metadata for a foreign key constraint in a database.
 * Contains information about the constraint name and referenced columns/tables.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKeyMetadata {

    /**
     * The name of the constraint.
     */
    private String constraintName;
    
    /**
     * The name of the column in the table that has the foreign key constraint.
     */
    private String columnName;
    
    /**
     * The name of the table being referenced by this foreign key.
     */
    private String referencedTable;
    
    /**
     * The name of the column being referenced by this foreign key.
     */
    private String referencedColumn;
    
    /**
     * The name of the table that has the foreign key constraint.
     */
    private String tableName;
    
    /**
     * The schema of the table that has the foreign key constraint.
     */
    private String schemaName;
    
    /**
     * The schema of the referenced table.
     */
    private String referencedTableSchema;
    
    /**
     * Gets the name of the table being referenced by this foreign key.
     * Alternative name for backward compatibility.
     *
     * @return the name of the referenced table
     */
    public String getReferencedTableName() {
        return referencedTable;
    }
    
    /**
     * Gets the name of the column being referenced by this foreign key.
     * Alternative name for backward compatibility.
     *
     * @return the name of the referenced column
     */
    public String getReferencedColumnName() {
        return referencedColumn;
    }
}

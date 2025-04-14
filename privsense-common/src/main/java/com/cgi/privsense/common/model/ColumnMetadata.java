package com.cgi.privsense.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents metadata for a database column.
 * Contains information about column structure, type, and constraints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata {

    /**
     * The name of the column.
     */
    private String name;
    
    /**
     * The data type of the column as defined in the database.
     */
    private String dataType;
    
    /**
     * The position of the column in the table (0-based).
     */
    private int ordinalPosition;
    
    /**
     * Whether the column allows NULL values.
     */
    private boolean nullable;
    
    /**
     * The maximum length for character data types.
     * This will be null for non-character types.
     */
    private Integer characterMaxLength;
    
    /**
     * Whether this column is part of the primary key.
     */
    private boolean isPrimaryKey;
    
    /**
     * Whether this column is auto-incremented.
     */
    private boolean isAutoIncrement;
    
    /**
     * The default value for the column, if any.
     */
    private String defaultValue;
    
    /**
     * The table to which this column belongs.
     */
    private String tableName;
    
    /**
     * The schema to which the table of this column belongs.
     */
    private String schemaName;
}

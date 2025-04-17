package com.cgi.privsense.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import java.util.HashMap;
import java.util.Map;

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
     * The default value for this column.
     */
    private String columnDefault;
    
    /**
     * The numeric precision for numeric data types.
     */
    private Integer numericPrecision;
    
    /**
     * The numeric scale for numeric data types.
     */
    private Integer numericScale;
    
    /**
     * Whether this column is auto-incremented.
     */
    private boolean isAutoIncrement;
    
    /**
     * The table to which this column belongs.
     */
    private String tableName;
    
    /**
     * The schema to which the table of this column belongs.
     */
    private String schemaName;

    /**
     * Additional database-specific properties
     * Using @Singular to generate additionalProperty() methods for the builder
     */
    @Singular
    private Map<String, String> additionalProperties = new HashMap<>();

    /**
     * Adds an additional property to the column metadata
     */
    public void addAdditionalProperty(String key, String value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        additionalProperties.put(key, value);
    }

    /**
     * Gets an additional property value
     */
    public String getAdditionalProperty(String key) {
        return additionalProperties != null ? additionalProperties.get(key) : null;
    }
    
    /**
     * Gets the normalized type name for the column.
     * This is a convenience method that returns the dataType.
     * 
     * @return The SQL type name of the column
     */
    public String getTypeName() {
        return dataType;
    }
}
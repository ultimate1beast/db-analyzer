package com.cgi.privsense.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for column metadata containing information about a database column.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadataDto {

    /**
     * Name of the column
     */
    private String name;
    
    /**
     * Name of the table containing this column
     */
    private String tableName;
    
    /**
     * SQL data type of the column
     */
    private String dataType;
    
    /**
     * Position of the column in the table (1-based)
     */
    private Integer ordinalPosition;
    
    /**
     * Whether the column allows NULL values
     */
    private Boolean nullable;
    
    /**
     * Maximum length for character columns
     */
    private Integer characterMaxLength;
    
    /**
     * Whether the column is part of the primary key
     */
    private Boolean isPrimaryKey;
    
    /**
     * Default value for the column
     */
    private String columnDefault;
    
    /**
     * Numeric precision for numeric columns
     */
    private Integer numericPrecision;
    
    /**
     * Numeric scale for numeric columns
     */
    private Integer numericScale;
}

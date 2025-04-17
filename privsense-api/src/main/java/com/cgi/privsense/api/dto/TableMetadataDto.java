package com.cgi.privsense.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for table metadata including information about columns, keys, and statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadataDto {

    /**
     * Name of the table
     */
    private String name;
    
    /**
     * Schema or database name containing the table
     */
    private String schema;
    
    /**
     * Approximate row count (may be an estimate from database statistics)
     */
    private Long rowCount;
    
    /**
     * Size of the table in bytes (may be an estimate)
     */
    private Long sizeInBytes;
    
    /**
     * Array of column metadata for all columns in the table
     */
    private ColumnMetadataDto[] columns;
    
    /**
     * Names of primary key columns
     */
    private String[] primaryKeys;
    
    /**
     * Foreign key relationships
     */
    private ForeignKeyMetadataDto[] foreignKeys;
}

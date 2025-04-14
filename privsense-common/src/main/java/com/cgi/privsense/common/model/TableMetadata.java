package com.cgi.privsense.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents metadata for a database table.
 * Contains information about table structure, size, and relationships.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata {

    /**
     * The name of the table.
     */
    private String name;
    
    /**
     * The schema to which the table belongs.
     */
    private String schema;
    
    /**
     * The estimated number of rows in the table.
     */
    private long rowCount;
    
    /**
     * The estimated size of the table in bytes.
     */
    private long sizeInBytes;
    
    /**
     * List of columns in the table.
     */
    @Builder.Default
    private List<ColumnMetadata> columns = new ArrayList<>();
    
    /**
     * List of column names that form the primary key.
     */
    @Builder.Default
    private List<String> primaryKeys = new ArrayList<>();
    
    /**
     * List of foreign keys defined on the table.
     */
    @Builder.Default
    private List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();
}

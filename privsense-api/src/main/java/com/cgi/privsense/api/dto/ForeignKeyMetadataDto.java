package com.cgi.privsense.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for foreign key metadata containing information about foreign key relationships.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKeyMetadataDto {

    /**
     * Name of the constraint
     */
    private String constraintName;
    
    /**
     * Name of the column that is a foreign key
     */
    private String columnName;
    
    /**
     * Name of the table containing the foreign key column
     */
    private String tableName;
    
    /**
     * Name of the referenced table
     */
    private String referencedTableName;
    
    /**
     * Name of the referenced column
     */
    private String referencedColumnName;
}

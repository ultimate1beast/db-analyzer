
package com.cgi.privsense.common.database;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing different types of databases supported by PrivSense.
 */
public enum DatabaseType {
    MYSQL,
    ORACLE,
    SQL_SERVER,
    POSTGRESQL,
    OTHER; // For generic JDBC or unsupported types


     /**
     * Creates a DatabaseType from a string value (case-insensitive).
     */
    @JsonCreator
    public static DatabaseType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        return Arrays.stream(DatabaseType.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown database type: " + value));
    }
    
    /**
     * Returns the string representation of this database type.
     */
    @JsonValue
    public String getValue() {
        return this.name();
    }
}
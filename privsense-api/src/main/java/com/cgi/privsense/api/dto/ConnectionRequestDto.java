package com.cgi.privsense.api.dto;

import com.cgi.privsense.common.database.DatabaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for database connection request parameters.
 * Contains all necessary information to establish a connection to a database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDto {

    /**
     * Type of database to connect to (e.g., MYSQL, POSTGRESQL, ORACLE, SQL_SERVER)
     */
    @NotNull(message = "Database type must not be null")
    private DatabaseType databaseType;

    /**
     * Map of connection parameters specific to the database type.
     * Common parameters include:
     * - host: Database host address
     * - port: Database port
     * - database: Database name
     * - username: Username for authentication
     * - password: Password for authentication
     * - schema: Schema name (optional)     * - additionalParams: Additional database-specific parameters
     */
    @NotNull(message = "Connection parameters must not be null")
    @Builder.Default
    private Map<String, String> connectionParams = new HashMap<>();

    /**
     * Optional connection pool configuration.
     * If not provided, default values will be used.
     */
    private ConnectionPoolConfigDto poolConfig;
}

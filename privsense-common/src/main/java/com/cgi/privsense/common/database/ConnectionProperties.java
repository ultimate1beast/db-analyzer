
package com.cgi.privsense.common.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the properties needed to establish a database connection,
 * including credentials, location, type, and pool configuration.
 */
@Data
@NoArgsConstructor
public class ConnectionProperties {

    private DatabaseType databaseType;
    private String host;
    private Integer port;
    private String database;
    private String schema; // Optional schema name
    private String username;
    private String password;

    /**
     * Additional connection parameters specific to the JDBC driver.
     * Example: useSSL=false, allowPublicKeyRetrieval=true for MySQL.
     */
    private Map<String, String> additionalParams = new HashMap<>();

    /**
     * Configuration for the connection pool.
     * If null, default pool settings will be used.
     */
    private PoolConfig poolConfig = new PoolConfig(); // Initialize with defaults

    /**
     * Convenience method to get a specific additional parameter.
     *
     * @param key The parameter key.
     * @return The parameter value, or null if not found.
     */
    public String getParam(String key) {
        return additionalParams.get(key);
    }

     /**
     * Convenience method to get a specific additional parameter with a default value.
     *
     * @param key The parameter key.
     * @param defaultValue The default value if the key is not found.
     * @return The parameter value or the default value.
     */
    public String getParamOrDefault(String key, String defaultValue) {
        String value = getParam(key);
        return value != null ? value : defaultValue;
    }
}
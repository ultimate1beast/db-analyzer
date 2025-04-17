package com.cgi.privsense.dbscanner.factory;

import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.database.DatabaseType;
import com.cgi.privsense.dbscanner.connector.MySqlConnectionProvider;

import java.util.Map;

/**
 * Factory for creating DatabaseConnectionProvider instances based on database type.
 * Uses the factory pattern to create the appropriate connection provider.
 */
public class DatabaseConnectionFactory {
    
    private DatabaseConnectionFactory() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Creates a DatabaseConnectionProvider for the specified database type.
     *
     * @param type The database type
     * @param connectionParams Connection parameters for the database
     * @return A DatabaseConnectionProvider instance for the specified database type
     * @throws IllegalArgumentException if the database type is not supported or parameters are invalid
     */
    public static DatabaseConnectionProvider createConnectionProvider(DatabaseType type, Map<String, String> connectionParams) {
        if (type == null) {
            throw new IllegalArgumentException("Database type cannot be null");
        }
        
        if (connectionParams == null || connectionParams.isEmpty()) {
            throw new IllegalArgumentException("Connection parameters cannot be null or empty");        }        switch (type) {
            case MYSQL:
                MySqlConnectionProvider provider = new MySqlConnectionProvider();
                return provider;
            case ORACLE:
            case SQL_SERVER:
            case POSTGRESQL:
            case OTHER:
                throw new UnsupportedOperationException("Support for " + type + " database is not implemented yet");
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
}

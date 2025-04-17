package com.cgi.privsense.api.config;

import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.service.MetadataExtractor;
import com.cgi.privsense.dbscanner.factory.DatabaseConnectionFactory;
import com.cgi.privsense.common.database.DatabaseType;
import com.cgi.privsense.dbscanner.metadata.JdbcMetadataExtractor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for database connections.
 * Provides beans for database connectivity following factory pattern and connection pooling best practices.
 */
@Configuration
public class DatabaseConfig {
    
    @Value("${privsense.db.pool.max-size:10}")
    private int maxPoolSize;
    
    @Value("${privsense.db.pool.idle-timeout:300000}")
    private long idleTimeout;
    
    @Value("${privsense.db.default-type:MYSQL}")
    private String defaultDatabaseType;
    
    @Value("${privsense.db.default.host:localhost}")
    private String defaultHost;
    
    @Value("${privsense.db.default.port:3306}")
    private String defaultPort;
    
    @Value("${privsense.db.default.database:}")
    private String defaultDatabase;
    
    @Value("${privsense.db.default.username:}")
    private String defaultUsername;
    
    @Value("${privsense.db.default.password:}")
    private String defaultPassword;
      /**
     * Creates connection providers for all supported database types.
     * This follows the best practice of dependency inversion by depending on the interface
     * rather than concrete implementations.
     * 
     * @return A map of database type to connection provider
     */
    @Bean
    public Map<DatabaseType, DatabaseConnectionProvider> databaseConnectionProviders() {
        Map<DatabaseType, DatabaseConnectionProvider> providers = new HashMap<>();
        
        // Initialize all supported database types from the factory
        for (DatabaseType dbType : DatabaseType.values()) {
            try {
                // Create a map of connection parameters
                Map<String, String> connectionParams = createConnectionParams(dbType);
                
                // Create the provider with the connection parameters
                DatabaseConnectionProvider provider = DatabaseConnectionFactory.createConnectionProvider(dbType, connectionParams);
                if (provider != null) {
                    providers.put(dbType, provider);
                }
            } catch (UnsupportedOperationException ex) {
                // Skip unsupported database types
                continue;
            }
        }
        
        return providers;
    }
    
    /**
     * Creates a map of connection parameters for the given database type.
     * Uses the configuration properties from application.properties.
     * 
     * @param dbType The database type
     * @return A map of connection parameters
     */
    private Map<String, String> createConnectionParams(DatabaseType dbType) {
        Map<String, String> params = new HashMap<>();
        
        // Add default connection parameters
        params.put("host", defaultHost);
        params.put("port", defaultPort);
        params.put("database", defaultDatabase);
        params.put("username", defaultUsername);
        params.put("password", defaultPassword);
        
        // Add connection pool settings
        params.put("maxPoolSize", String.valueOf(maxPoolSize));
        params.put("idleTimeout", String.valueOf(idleTimeout));
        
        return params;
    }/**
     * Creates a default DatabaseConnectionProvider bean.
     * This bean is required by various services like DatabaseMetadataService.
     * Uses the factory pattern to create the appropriate provider based on configuration.
     * 
     * @return The default DatabaseConnectionProvider instance
     */
    @Bean
    @Primary
    public DatabaseConnectionProvider defaultDatabaseConnectionProvider(
            Map<DatabaseType, DatabaseConnectionProvider> providers) {
        
        DatabaseType dbType;
        try {
            dbType = DatabaseType.valueOf(defaultDatabaseType);
        } catch (IllegalArgumentException e) {
            dbType = DatabaseType.MYSQL; // Default to MySQL if configuration is invalid
        }
        
        DatabaseConnectionProvider provider = providers.get(dbType);
        if (provider == null) {
            throw new IllegalStateException("No connection provider available for database type: " + dbType);
        }
        
        return provider;
    }
      /**
     * Creates an in-memory H2 DataSource for application startup.
     * This allows the application to start without requiring an external database connection.
     * Real database connections can be configured later via API calls.
     * 
     * @return The default in-memory DataSource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        // Use H2 in-memory database to ensure the application can start without external dependencies
        config.setJdbcUrl("jdbc:h2:mem:privsensedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(maxPoolSize);
        config.setIdleTimeout(idleTimeout);
        
        return new HikariDataSource(config);
    }
    
    /**
     * Creates a new DataSource based on provided connection parameters.
     * This method is used by the API when a user requests to connect to a specific database.
     * 
     * @param connectionParams Map of connection parameters
     * @return A new DataSource for the requested database
     */
    public DataSource createDynamicDataSource(Map<String, String> connectionParams) {
        String dbTypeStr = connectionParams.getOrDefault("databaseType", "MYSQL");
        DatabaseType dbType;
        try {
            dbType = DatabaseType.valueOf(dbTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            dbType = DatabaseType.MYSQL;
        }
        
        String host = connectionParams.getOrDefault("host", "localhost");
        String port = connectionParams.getOrDefault("port", getDefaultPort(dbType));
        String database = connectionParams.getOrDefault("database", "");
        String username = connectionParams.getOrDefault("username", "");
        String password = connectionParams.getOrDefault("password", "");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildJdbcUrl(dbType, host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setIdleTimeout(idleTimeout);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return new HikariDataSource(config);
    }
    
    private String getDefaultPort(DatabaseType dbType) {
        switch (dbType) {
            case MYSQL:
                return "3306";
            case POSTGRESQL:
                return "5432";
            case SQL_SERVER:
                return "1433";
            case ORACLE:
                return "1521";
            default:
                return "3306";
        }
    }
      private String buildJdbcUrl(DatabaseType dbType, String host, String port, String database) {
        switch (dbType) {
            case MYSQL:
                return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC",
                        host, port, database);
            case POSTGRESQL:
                return String.format("jdbc:postgresql://%s:%s/%s",
                        host, port, database);
            case SQL_SERVER:
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s",
                        host, port, database);
            case ORACLE:
                return String.format("jdbc:oracle:thin:@%s:%s:%s",
                        host, port, database);
            default:
                throw new IllegalStateException("Unsupported database type for connection: " + dbType);
        }
    }
    
    /**
     * Creates a MetadataExtractor bean that can be used to extract metadata from databases.
     * This bean is required by the DatabaseMetadataService.
     * 
     * @param connectionProvider The default database connection provider
     * @return A MetadataExtractor implementation
     */
    @Bean
    public MetadataExtractor metadataExtractor(DatabaseConnectionProvider defaultDatabaseConnectionProvider) {
        // Create a map of connection parameters for the metadata extractor
        Map<String, String> connectionParams = new HashMap<>();
        connectionParams.put("host", defaultHost);
        connectionParams.put("port", defaultPort);
        connectionParams.put("database", defaultDatabase);
        connectionParams.put("username", defaultUsername);
        connectionParams.put("password", defaultPassword);
        
        // Create and return a JdbcMetadataExtractor
        return new JdbcMetadataExtractor(defaultDatabaseConnectionProvider, connectionParams);
    }
}


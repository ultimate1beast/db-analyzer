package com.cgi.privsense.dbscanner.connector;

import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.database.DatabaseType;
import com.cgi.privsense.common.exception.DatabaseConnectionException;

import com.cgi.privsense.common.util.ValidationUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.cgi.privsense.common.database.PoolConfig;
import javax.sql.DataSource;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MySQL specific implementation of DatabaseConnectionProvider.
 * Uses HikariCP for connection pooling.
 */
public class MySqlConnectionProvider implements DatabaseConnectionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(MySqlConnectionProvider.class.getName());
    private static final String PARAM_URL = "url";
    private static final String PARAM_HOST = "host";
    private static final String PARAM_PORT = "port";
    private static final String PARAM_DATABASE = "database";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";
    private static final String DEFAULT_PORT = "3306";
    
    private final ConcurrentHashMap<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
      /**
     * Creates a new MySqlConnectionProvider.
     */
    public MySqlConnectionProvider() {
        // Constructor is now empty as we create DataSources on demand
    }
    
    @Override
    public DataSource getDataSource(ConnectionProperties properties) throws DatabaseConnectionException {
        ValidationUtils.notNull(properties, "Connection properties cannot be null");
        
        // Create a unique key for this connection
        String connectionKey = createConnectionKey(properties);
        
        // Return existing DataSource if we have one
        return dataSources.computeIfAbsent(connectionKey, key -> {
            HikariConfig config = createHikariConfig(properties);
            try {
                return new HikariDataSource(config);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to create MySQL DataSource", e);
                throw new DatabaseConnectionException("Failed to create MySQL DataSource", e);
            }
        });
    }
    
    // We use the default implementation from the interface
    // @Override
    // public void closeConnection(Connection connection) {
    //     DatabaseUtils.closeConnection(connection);
    // }
      @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }
    
    @Override
    public void closePool() {
        // Close all data sources
        for (HikariDataSource ds : dataSources.values()) {
            if (ds != null && !ds.isClosed()) {
                try {
                    ds.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error closing MySQL connection pool", e);
                }
            }
        }
        dataSources.clear();
    }
    
    /**
     * Creates a unique key for a connection based on its properties.
     *
     * @param properties the connection properties
     * @return a unique key representing this connection
     */    private String createConnectionKey(ConnectionProperties properties) {
        // Create a unique identifier based on connection parameters
        String host = properties.getHost();        String port = properties.getPort() != null ? properties.getPort().toString() : DEFAULT_PORT;
        String database = properties.getDatabase();
        String username = properties.getUsername();
        
        return String.format("%s:%s:%s:%s", host, port, database, username);
    }
    
    /**
     * Creates HikariCP configuration from the connection properties.
     *
     * @param properties the connection properties
     * @return configured HikariConfig
     */    private HikariConfig createHikariConfig(ConnectionProperties properties) {
        HikariConfig config = new HikariConfig();
        
        // Set JDBC URL
        String jdbcUrl;        // Check if there's a custom JDBC URL in additional parameters
        if (properties.getAdditionalParams().containsKey("jdbcUrl")) {
            jdbcUrl = properties.getAdditionalParams().get("jdbcUrl");
        } else {
            String host = properties.getHost();
            String port = properties.getPort() != null ? properties.getPort().toString() : DEFAULT_PORT;
            String database = properties.getDatabase();
            jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
        }
        config.setJdbcUrl(jdbcUrl);
          // Set credentials
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword() != null ? properties.getPassword() : "");
          // Configure pool
        String poolName = "privsense-mysql-" + properties.getDatabase();
        config.setPoolName(poolName);
        
        // Get pool config
        PoolConfig poolConfig = properties.getPoolConfig();
        
        // Use pool config from properties if provided, otherwise defaults are used
        config.setMaximumPoolSize(poolConfig.getMaxPoolSize());
        config.setMinimumIdle(poolConfig.getMinIdleConnections());
        config.setIdleTimeout(poolConfig.getIdleTimeout());
        config.setMaxLifetime(poolConfig.getConnectionMaxLifetime());
        config.setConnectionTimeout(poolConfig.getConnectionTimeout());          // MySQL specific settings
        Properties props = new Properties();
        props.setProperty("useSSL", "false");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("characterEncoding", "utf8");
        props.setProperty("useUnicode", "true");
        props.setProperty("rewriteBatchedStatements", "true");
        props.setProperty("cachePrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        props.setProperty("useServerPrepStmts", "true");
        
        // Add any additional properties from the connection properties
        if (properties.getAdditionalParams() != null) {
            properties.getAdditionalParams().forEach(props::setProperty);
        }
        
        config.setDataSourceProperties(props);
        
        return config;
    }
}

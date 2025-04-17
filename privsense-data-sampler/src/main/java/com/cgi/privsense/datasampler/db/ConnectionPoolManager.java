package com.cgi.privsense.datasampler.db;

import com.cgi.privsense.common.exception.DatabaseConnectionException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages database connection pooling for efficient database access during sampling operations.
 * Uses HikariCP for high-performance connection pooling, optimized for sampling large databases.
 */
public class ConnectionPoolManager {

    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    
    // Default pool settings optimized for sampling operations on large databases
    private static final int DEFAULT_MAX_POOL_SIZE = 20;
    private static final int DEFAULT_MIN_IDLE = 5;
    private static final long DEFAULT_CONNECTION_TIMEOUT_MS = 30000; // 30 seconds
    private static final long DEFAULT_IDLE_TIMEOUT_MS = 600000; // 10 minutes
    private static final long DEFAULT_MAX_LIFETIME_MS = 1800000; // 30 minutes
    
    // Maximum allowed pool size (to prevent resource exhaustion)
    private static final int ABSOLUTE_MAX_POOL_SIZE = 50;
    
    /**
     * Creates a connection pool for the given database configuration.
     *
     * @param dbId a unique identifier for this database connection pool
     * @param jdbcUrl the JDBC URL for the database connection
     * @param username the database username
     * @param password the database password 
     * @return a connection pool manager instance
     */
    public synchronized HikariDataSource createConnectionPool(
            String dbId, String jdbcUrl, String username, String password) {
        return createConnectionPool(dbId, jdbcUrl, username, password, DEFAULT_MAX_POOL_SIZE);
    }
    
    /**
     * Creates a connection pool for the given database configuration with a custom pool size.
     *
     * @param dbId a unique identifier for this database connection pool
     * @param jdbcUrl the JDBC URL for the database connection
     * @param username the database username
     * @param password the database password
     * @param maxPoolSize the maximum size of the connection pool
     * @return a connection pool manager instance
     */
    public synchronized HikariDataSource createConnectionPool(
            String dbId, String jdbcUrl, String username, String password, int maxPoolSize) {
        
        // Check if a pool already exists for this database
        if (dataSources.containsKey(dbId)) {
            return dataSources.get(dbId);
        }
        
        // Validate pool size and cap it if necessary
        int actualPoolSize = Math.min(maxPoolSize, ABSOLUTE_MAX_POOL_SIZE);
        
        // Create HikariCP configuration
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // Pool size configuration
        config.setMaximumPoolSize(actualPoolSize);
        config.setMinimumIdle(Math.min(DEFAULT_MIN_IDLE, actualPoolSize));
        
        // Timeout settings
        config.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(DEFAULT_IDLE_TIMEOUT_MS);
        config.setMaxLifetime(DEFAULT_MAX_LIFETIME_MS);
        
        // Performance optimizations for sampling large databases
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // Additional tuning based on database type (derived from JDBC URL)
        if (jdbcUrl.contains("mysql")) {
            // MySQL specific optimizations
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
        } else if (jdbcUrl.contains("postgresql")) {
            // PostgreSQL specific optimizations
            config.addDataSourceProperty("useServerPrepStmts", "true");
        } else if (jdbcUrl.contains("oracle")) {
            // Oracle specific optimizations
            config.addDataSourceProperty("implicitCachingEnabled", "true");
            config.addDataSourceProperty("oracle.jdbc.ReadTimeout", "60000");
        } else if (jdbcUrl.contains("sqlserver")) {
            // SQL Server specific optimizations
            config.addDataSourceProperty("sendStringParametersAsUnicode", "false");
        }
        
        // Create the data source
        HikariDataSource dataSource = new HikariDataSource(config);
        dataSources.put(dbId, dataSource);
        
        return dataSource;
    }
    
    /**
     * Gets a connection from the specified database pool.
     *
     * @param dbId the database identifier used when creating the pool
     * @return a connection from the pool
     * @throws DatabaseConnectionException if the pool doesn't exist or a connection cannot be obtained
     */
    public Connection getConnection(String dbId) {
        HikariDataSource dataSource = dataSources.get(dbId);
        if (dataSource == null) {
            throw new DatabaseConnectionException("No connection pool exists for database ID: " + dbId);
        }
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to get connection from pool: " + e.getMessage(), e);
        }
    }
    
    /**
     * Returns the current size of the connection pool.
     *
     * @param dbId the database identifier used when creating the pool
     * @return the number of active connections in the pool
     * @throws DatabaseConnectionException if the pool doesn't exist
     */
    public int getActiveConnections(String dbId) {
        HikariDataSource dataSource = dataSources.get(dbId);
        if (dataSource == null) {
            throw new DatabaseConnectionException("No connection pool exists for database ID: " + dbId);
        }
        
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }
    
    /**
     * Gets statistics about all managed connection pools.
     *
     * @return a map of database IDs to connection statistics
     */
    public Map<String, PoolStatistics> getPoolStatistics() {
        Map<String, PoolStatistics> stats = new HashMap<>();
        
        for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {
            String dbId = entry.getKey();
            HikariDataSource ds = entry.getValue();
            
            PoolStatistics poolStats = new PoolStatistics(
                ds.getHikariPoolMXBean().getActiveConnections(),
                ds.getHikariPoolMXBean().getIdleConnections(),
                ds.getHikariPoolMXBean().getTotalConnections(),
                ds.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
            
            stats.put(dbId, poolStats);
        }
        
        return stats;
    }
    
    /**
     * Closes a specific connection pool, releasing all resources.
     *
     * @param dbId the database identifier used when creating the pool
     */
    public synchronized void closePool(String dbId) {
        HikariDataSource dataSource = dataSources.remove(dbId);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * Closes all connection pools managed by this instance.
     * Should be called when the application is shutting down.
     */
    public synchronized void closeAllPools() {
        for (HikariDataSource dataSource : dataSources.values()) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        }
        dataSources.clear();
    }
    
    /**
     * Inner class to hold statistics about a connection pool.
     */
    public static class PoolStatistics {
        private final int activeConnections;
        private final int idleConnections;
        private final int totalConnections;
        private final int waitingThreads;
        
        public PoolStatistics(int activeConnections, int idleConnections, int totalConnections, int waitingThreads) {
            this.activeConnections = activeConnections;
            this.idleConnections = idleConnections;
            this.totalConnections = totalConnections;
            this.waitingThreads = waitingThreads;
        }
        
        public int getActiveConnections() {
            return activeConnections;
        }
        
        public int getIdleConnections() {
            return idleConnections;
        }
        
        public int getTotalConnections() {
            return totalConnections;
        }
        
        public int getWaitingThreads() {
            return waitingThreads;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Pool Statistics [active: %d, idle: %d, total: %d, waiting: %d]",
                activeConnections, idleConnections, totalConnections, waitingThreads
            );
        }
    }
}

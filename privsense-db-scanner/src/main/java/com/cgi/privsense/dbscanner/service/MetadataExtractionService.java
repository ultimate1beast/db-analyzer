package com.cgi.privsense.dbscanner.service;

import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.database.DatabaseType;
import com.cgi.privsense.common.exception.MetadataExtractionException;
import com.cgi.privsense.common.model.TableMetadata;
import com.cgi.privsense.common.service.MetadataExtractor;
import com.cgi.privsense.common.util.ValidationUtils;
import com.cgi.privsense.dbscanner.factory.DatabaseConnectionFactory;
import com.cgi.privsense.dbscanner.metadata.JdbcMetadataExtractor;
import com.cgi.privsense.dbscanner.metadata.MySqlMetadataExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for metadata extraction operations.
 * Provides caching and parallel extraction capabilities.
 */
public class MetadataExtractionService {
    
    private static final Logger LOGGER = Logger.getLogger(MetadataExtractionService.class.getName());
    private static final int DEFAULT_PARALLEL_THREADS = 4;
    
    private final DatabaseConnectionFactory connectionFactory;
    private final Map<String, TableMetadata> metadataCache;
    private final ExecutorService executorService;
    private final int maxThreads;
    
    /**
     * Creates a new MetadataExtractionService with default thread pool size.
     *
     * @param connectionFactory The database connection factory
     */
    public MetadataExtractionService(DatabaseConnectionFactory connectionFactory) {
        this(connectionFactory, DEFAULT_PARALLEL_THREADS);
    }
    
    /**
     * Creates a new MetadataExtractionService with specified thread pool size.
     *
     * @param connectionFactory The database connection factory
     * @param maxThreads Maximum number of threads for parallel extraction
     */
    public MetadataExtractionService(DatabaseConnectionFactory connectionFactory, int maxThreads) {
        ValidationUtils.notNull(connectionFactory, "Connection factory cannot be null");
        this.connectionFactory = connectionFactory;
        this.metadataCache = new ConcurrentHashMap<>();
        this.maxThreads = maxThreads > 0 ? maxThreads : DEFAULT_PARALLEL_THREADS;
        this.executorService = Executors.newFixedThreadPool(this.maxThreads);
    }
    
    /**
     * Extracts metadata for all tables in the database.
     *
     * @param databaseType The type of database
     * @param connectionParams Connection parameters
     * @return List of table metadata
     */
    public List<TableMetadata> extractAllTablesMetadata(DatabaseType databaseType, Map<String, String> connectionParams) {
        ValidationUtils.notNull(databaseType, "Database type cannot be null");
        ValidationUtils.notNull(connectionParams, "Connection parameters cannot be null");
        
        LOGGER.info("Extracting metadata for all tables from " + databaseType + " database");
        MetadataExtractor extractor = createMetadataExtractor(databaseType, connectionParams);
        
        List<TableMetadata> tableMetadataList = extractor.extractAllTablesMetadata();
        
        // Cache results
        for (TableMetadata metadata : tableMetadataList) {
            String cacheKey = buildCacheKey(databaseType, connectionParams, metadata.getName());
            metadataCache.put(cacheKey, metadata);
        }
        
        return tableMetadataList;
    }
    
    /**
     * Extracts metadata for a specific table.
     *
     * @param databaseType The type of database
     * @param connectionParams Connection parameters
     * @param tableName The name of the table
     * @param useCache Whether to use cached metadata if available
     * @return Table metadata
     */
    public TableMetadata extractTableMetadata(DatabaseType databaseType, Map<String, String> connectionParams, 
            String tableName, boolean useCache) {
        ValidationUtils.notNull(databaseType, "Database type cannot be null");
        ValidationUtils.notNull(connectionParams, "Connection parameters cannot be null");
        ValidationUtils.notEmpty(tableName, "Table name cannot be empty");
        
        String cacheKey = buildCacheKey(databaseType, connectionParams, tableName);
        
        // Check cache first if enabled
        if (useCache && metadataCache.containsKey(cacheKey)) {
            LOGGER.info("Using cached metadata for table: " + tableName);
            return metadataCache.get(cacheKey);
        }
        
        LOGGER.info("Extracting metadata for table: " + tableName + " from " + databaseType + " database");
        MetadataExtractor extractor = createMetadataExtractor(databaseType, connectionParams);
        
        TableMetadata metadata = extractor.extractTableMetadata(tableName);
        
        // Cache the result
        metadataCache.put(cacheKey, metadata);
        
        return metadata;
    }
    
    /**
     * Extracts metadata for tables matching a pattern in parallel.
     *
     * @param databaseType The type of database
     * @param connectionParams Connection parameters
     * @param pattern Regular expression pattern for table names
     * @return List of table metadata
     */
    public List<TableMetadata> extractTablesMetadataInParallel(DatabaseType databaseType, 
            Map<String, String> connectionParams, String pattern) {
        ValidationUtils.notNull(databaseType, "Database type cannot be null");
        ValidationUtils.notNull(connectionParams, "Connection parameters cannot be null");
        ValidationUtils.notEmpty(pattern, "Pattern cannot be empty");
          LOGGER.info("Extracting metadata for tables matching pattern: " + pattern);
        MetadataExtractor extractor = createMetadataExtractor(databaseType, connectionParams);
        
        // First get all tables matching the pattern
        List<String> tableNames = extractor.searchTables(pattern).stream()
                .map(TableMetadata::getName)
                .collect(Collectors.toList());
        
        if (tableNames.isEmpty()) {
            LOGGER.info("No tables found matching pattern: " + pattern);
            return new ArrayList<>();
        }
        
        // Process tables in parallel
        List<Future<TableMetadata>> futures = new ArrayList<>();
        
        for (String tableName : tableNames) {
            String cacheKey = buildCacheKey(databaseType, connectionParams, tableName);
            
            // Skip if already cached
            if (metadataCache.containsKey(cacheKey)) {
                continue;
            }
            
            // Submit extraction task to thread pool
            futures.add(executorService.submit(() -> {
                MetadataExtractor threadExtractor = createMetadataExtractor(databaseType, connectionParams);
                TableMetadata metadata = threadExtractor.extractTableMetadata(tableName);
                metadataCache.put(cacheKey, metadata);
                return metadata;
            }));
        }
        
        // Collect results
        List<TableMetadata> results = new ArrayList<>();
        
        // Add cached results
        for (String tableName : tableNames) {
            String cacheKey = buildCacheKey(databaseType, connectionParams, tableName);
            if (metadataCache.containsKey(cacheKey)) {
                results.add(metadataCache.get(cacheKey));
            }
        }
        
        // Add newly extracted results
        for (Future<TableMetadata> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.log(Level.WARNING, "Error extracting table metadata in parallel", e);
                // Continue with other tables even if one fails
            }
        }
        
        return results;
    }
    
    /**
     * Clears the metadata cache.
     */
    public void clearCache() {
        LOGGER.info("Clearing metadata cache");
        metadataCache.clear();
    }
    
    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        LOGGER.info("Shutting down metadata extraction service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Creates a metadata extractor for the specified database type.
     *
     * @param databaseType The type of database
     * @param connectionParams Connection parameters
     * @return MetadataExtractor instance
     */
    private MetadataExtractor createMetadataExtractor(DatabaseType databaseType, Map<String, String> connectionParams) {
        DatabaseConnectionProvider connectionProvider = connectionFactory.createConnectionProvider(databaseType, connectionParams);
        
        // Create database-specific extractor based on the database type
        switch (databaseType) {
            case MYSQL:
                return new MySqlMetadataExtractor(connectionProvider, connectionParams);
            // Add other database types when they're implemented
            default:
                return new JdbcMetadataExtractor(connectionProvider, connectionParams);
        }
    }
    
    /**
     * Builds a cache key from the database type, connection parameters, and table name.
     *
     * @param databaseType The type of database
     * @param connectionParams Connection parameters
     * @param tableName The name of the table
     * @return Cache key string
     */
    private String buildCacheKey(DatabaseType databaseType, Map<String, String> connectionParams, String tableName) {
        String connectionString;
        
        if (connectionParams.containsKey("url")) {
            connectionString = connectionParams.get("url");
        } else if (connectionParams.containsKey("host") && connectionParams.containsKey("database")) {
            connectionString = connectionParams.get("host") + ":" + 
                    connectionParams.getOrDefault("port", "default") + "/" + 
                    connectionParams.get("database");
        } else {
            connectionString = "unknown";
        }
        
        return databaseType.name() + ":" + connectionString + ":" + tableName;
    }
}

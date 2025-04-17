package com.cgi.privsense.datasampler.strategy;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.model.SampleStatistics;
import com.cgi.privsense.datasampler.base.AbstractDataSampler;
import com.cgi.privsense.datasampler.db.ConnectionPoolManager;
import com.cgi.privsense.common.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of DataSampler that uses parallel processing for efficient sampling of large tables.
 * Divides the sampling task into chunks that are processed concurrently for better performance.
 */
public class ParallelSampler extends AbstractDataSampler {

    // Thread pool configuration
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService;
    
    // Chunk configuration
    private static final int DEFAULT_CHUNK_COUNT = 10;
    private final int chunkCount;
    
    // Timeout configuration
    private static final long DEFAULT_TIMEOUT_SECONDS = 60;
    private final long timeoutSeconds;
    
    // Connection pool management
    private final ConnectionPoolManager connectionPoolManager;
    private final String databaseId;
      /**
     * Constructs a ParallelSampler with default settings.
     * 
     * @param connectionPoolManager the connection pool manager to use
     * @param databaseId the database ID used for connection pool lookup
     */
    public ParallelSampler(ConnectionPoolManager connectionPoolManager, String databaseId) {
        this(connectionPoolManager, databaseId, DEFAULT_THREAD_POOL_SIZE, DEFAULT_CHUNK_COUNT, DEFAULT_TIMEOUT_SECONDS);
    }
    
    /**
     * Constructs a ParallelSampler with custom settings.
     *
     * @param connectionPoolManager the connection pool manager to use
     * @param databaseId the database ID used for connection pool lookup
     * @param threadPoolSize the size of the thread pool to use
     * @param chunkCount the number of chunks to divide sampling into
     * @param timeoutSeconds the timeout for sampling operations in seconds
     */
    public ParallelSampler(
            ConnectionPoolManager connectionPoolManager, 
            String databaseId,
            int threadPoolSize, 
            int chunkCount, 
            long timeoutSeconds) {
        this.connectionPoolManager = connectionPoolManager;
        this.databaseId = databaseId;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.chunkCount = chunkCount;
        this.timeoutSeconds = timeoutSeconds;
    }
      /**
     * Implements the abstract method from AbstractDataSampler.
     * Performs the actual sampling operation using parallel processing.
     *
     * @param tableName the name of the table
     * @param columnMetadata metadata for the column to sample
     * @param sampleSize the number of samples to retrieve
     * @return the list of sampled values
     */
    @Override
    protected List<String> performSampling(String tableName, ColumnMetadata columnMetadata, int sampleSize) {
        try {
            // Calculate chunk size
            int chunkSize = (sampleSize + chunkCount - 1) / chunkCount; // Ceiling division
            
            // Create tasks for each chunk
            List<CompletableFuture<ChunkResult>> chunkFutures = new ArrayList<>();
            
            for (int i = 0; i < chunkCount; i++) {
                final int chunkIndex = i;
                CompletableFuture<ChunkResult> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return sampleChunk(tableName, columnMetadata, chunkIndex, chunkSize);
                    } catch (Exception e) {
                        System.err.println("Error sampling chunk " + chunkIndex + ": " + e.getMessage());
                        return new ChunkResult(new ArrayList<>(), 0);
                    }
                }, executorService);
                
                chunkFutures.add(future);
            }
            
            // Wait for all futures to complete or timeout
            CompletableFuture<List<ChunkResult>> allFutures = CompletableFuture
                    .allOf(chunkFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> chunkFutures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()));
            
            List<ChunkResult> chunkResults = allFutures.get(timeoutSeconds, TimeUnit.SECONDS);
            
            // Aggregate results from all chunks
            List<String> combinedSamples = new ArrayList<>();
            
            for (ChunkResult chunkResult : chunkResults) {
                combinedSamples.addAll(chunkResult.samples);
            }
            
            // Limit to requested sample size (in case we got more)
            if (combinedSamples.size() > sampleSize) {
                combinedSamples = combinedSamples.subList(0, sampleSize);
            }
            
            return combinedSamples;
            
        } catch (Exception e) {
            throw new RuntimeException("Error during parallel sampling of " + 
                    tableName + "." + columnMetadata.getName(), e);
        }
    }
    
    /**
     * Samples a chunk of data from the specified table and column.
     *
     * @param tableName the name of the table
     * @param columnMetadata the metadata for the column to sample
     * @param chunkIndex the index of the chunk to sample
     * @param chunkSize the number of samples to retrieve for this chunk
     * @return a ChunkResult containing the samples and the number of rows scanned
     * @throws SQLException if a database error occurs
     */
    private ChunkResult sampleChunk(
            String tableName, ColumnMetadata columnMetadata, int chunkIndex, int chunkSize) throws SQLException {
        
        List<String> samples = new ArrayList<>();
        long rowsScanned = 0;
        
        // Use a database-specific random sampling query with chunk offset
        // This is a simplified example - real implementation would use database-specific optimization
        
        String columnName = columnMetadata.getName();
        String query = String.format(
                "SELECT %s FROM %s TABLESAMPLE SYSTEM(%d) OFFSET %d ROWS FETCH NEXT %d ROWS ONLY",
                columnName, tableName, 10, chunkIndex * chunkSize, chunkSize);
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String value = rs.getString(1);
                samples.add(value);
                rowsScanned++;
            }
        }
        
        return new ChunkResult(samples, rowsScanned);
    }
      /**
     * Calculates statistics for the given samples.
     *
     * @param samples the list of samples to analyze
     * @return the calculated statistics
     */
    @Override
    protected SampleStatistics calculateStatistics(List<String> samples) {
        // Count distinct values and their frequency
        Map<String, Integer> valueDistribution = new HashMap<>();
        int nullCount = 0;
        
        for (String sample : samples) {
            if (sample == null) {
                nullCount++;
            } else {
                valueDistribution.put(sample, valueDistribution.getOrDefault(sample, 0) + 1);
            }
        }
        
        // Calculate null percentage
        double nullPercentage = samples.isEmpty() ? 0 : (nullCount * 100.0) / samples.size();
        
        // Find min and max values (for orderable types)
        String minValue = null;
        String maxValue = null;
        
        if (!valueDistribution.isEmpty()) {
            List<String> nonNullValues = new ArrayList<>(valueDistribution.keySet());
            try {
                nonNullValues.sort(String::compareTo);
                minValue = nonNullValues.get(0);
                maxValue = nonNullValues.get(nonNullValues.size() - 1);
            } catch (Exception e) {
                // If values cannot be compared (e.g., non-orderable types), leave min/max as null
                System.err.println("Cannot determine min/max values: " + e.getMessage());
            }
        }
        
        // Build and return the statistics
        return SampleStatistics.builder()
                .distinctValueCount(valueDistribution.size())
                .nullCount(nullCount)
                .nullPercentage(nullPercentage)
                .minValue(minValue)
                .maxValue(maxValue)
                .valueDistribution(valueDistribution)
                .build();
    }
      /**
     * Gets a database connection for sampling from the connection pool.
     *
     * @return a database connection
     * @throws SQLException if a database error occurs
     */
    private Connection getConnection() throws SQLException {
        try {
            return connectionPoolManager.getConnection(databaseId);
        } catch (DatabaseConnectionException e) {
            throw new SQLException("Failed to get connection from pool", e);
        }
    }
    
    /**
     * Shuts down the executor service used for parallel sampling.
     * Should be called when the sampler is no longer needed.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Inner class representing the result of sampling a chunk of data.
     */
    private static class ChunkResult {
        private final List<String> samples;
        private final long rowsScanned;
        
        public ChunkResult(List<String> samples, long rowsScanned) {
            this.samples = samples;
            this.rowsScanned = rowsScanned;
        }
    }
}

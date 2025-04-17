package com.cgi.privsense.datasampler.service;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.model.SampleStatistics;
import com.cgi.privsense.common.service.DataSampler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service for optimized data sampling operations, particularly for large databases.
 * Provides parallel sampling capabilities and performance optimizations.
 */
public class DataSamplerService {

    private final DataSampler defaultSampler;
    private final SamplerFactory samplerFactory;
    private final Map<String, SampleResult> sampleCache = new ConcurrentHashMap<>();
    
    // Thread pool configuration
    private static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();
    private static final int MAX_PARALLEL_SAMPLING = Math.max(4, DEFAULT_PARALLELISM * 2);
    private final ExecutorService executorService;
    
    // Timeout settings
    private static final long DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutes
    private final long timeoutSeconds;
    
    // Adaptive batch size
    private static final int DEFAULT_BATCH_SIZE = 10;
    private final AtomicInteger currentBatchSize = new AtomicInteger(DEFAULT_BATCH_SIZE);
    
    /**
     * Constructs a DataSamplerService with the provided sampler and default settings.
     * 
     * @param defaultSampler the default sampler to use
     * @param samplerFactory the factory to create specialized samplers
     */
    public DataSamplerService(DataSampler defaultSampler, SamplerFactory samplerFactory) {
        this(defaultSampler, samplerFactory, MAX_PARALLEL_SAMPLING, DEFAULT_TIMEOUT_SECONDS);
    }
    
    /**
     * Constructs a DataSamplerService with custom thread pool size and timeout.
     * 
     * @param defaultSampler the default sampler to use
     * @param samplerFactory the factory to create specialized samplers
     * @param maxThreads the maximum number of threads to use
     * @param timeoutSeconds the timeout for sampling operations in seconds
     */
    public DataSamplerService(DataSampler defaultSampler, SamplerFactory samplerFactory, 
                             int maxThreads, long timeoutSeconds) {
        this.defaultSampler = defaultSampler;
        this.samplerFactory = samplerFactory;
        this.executorService = Executors.newFixedThreadPool(maxThreads);
        this.timeoutSeconds = timeoutSeconds;
    }
    
    /**
     * Samples multiple columns from a table in parallel.
     * 
     * @param tableName the name of the table
     * @param columns the list of columns to sample
     * @param sampleSize the number of samples to retrieve per column
     * @return a map of column names to sample results
     */
    public Map<String, SampleResult> sampleColumns(String tableName, List<ColumnMetadata> columns, int sampleSize) {
        // Create a map to store results
        Map<String, SampleResult> results = new HashMap<>();
        
        // Create a list to store futures
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // Submit sampling tasks for each column
        for (ColumnMetadata column : columns) {
            // Create a key for the cache
            String cacheKey = getCacheKey(tableName, column.getName(), sampleSize);
            
            // Check if the result is already in the cache
            if (sampleCache.containsKey(cacheKey)) {
                results.put(column.getName(), sampleCache.get(cacheKey));
                continue;
            }
            
            // Create a future for the column
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Get the appropriate sampler for the column
                    DataSampler sampler = getBestSamplerForColumn(column);
                    
                    // Sample the column
                    SampleResult result = sampler.sampleColumn(tableName, column, sampleSize);
                    
                    // Add the result to the map and cache
                    results.put(column.getName(), result);
                    sampleCache.put(cacheKey, result);
                } catch (Exception e) {
                    // Log the exception and continue with other columns
                    System.err.println("Error sampling column " + column.getName() + ": " + e.getMessage());
                }
            }, executorService);
            
            // Add the future to the list
            futures.add(future);
        }
        
        // Wait for all futures to complete or timeout
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            allFutures.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Timeout or error while sampling columns: " + e.getMessage());
            // Cancel any remaining tasks
            futures.forEach(future -> future.cancel(true));
        }
        
        // Adjust batch size based on results
        adjustBatchSizeBasedOnPerformance(results);
        
        return results;
    }
    
    /**
     * Samples multiple tables in batches for improved performance with very large schemas.
     * 
     * @param tableColumns a map of table names to lists of columns to sample
     * @param sampleSize the number of samples to retrieve per column
     * @return a map of table names to maps of column names to sample results
     */
    public Map<String, Map<String, SampleResult>> sampleMultipleTables(
            Map<String, List<ColumnMetadata>> tableColumns, int sampleSize) {
        
        Map<String, Map<String, SampleResult>> results = new ConcurrentHashMap<>();
        
        // Get all table names
        List<String> tableNames = new ArrayList<>(tableColumns.keySet());
        
        // Process tables in batches to avoid overwhelming the database
        int batchSize = currentBatchSize.get();
        for (int i = 0; i < tableNames.size(); i += batchSize) {
            // Get the current batch of tables
            int endIndex = Math.min(i + batchSize, tableNames.size());
            List<String> batchTables = tableNames.subList(i, endIndex);
            
            // Create a list to store futures for this batch
            List<CompletableFuture<Void>> batchFutures = new ArrayList<>();
            
            // Process each table in this batch
            for (String tableName : batchTables) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Sample all columns for this table
                        Map<String, SampleResult> tableResults = 
                                sampleColumns(tableName, tableColumns.get(tableName), sampleSize);
                        
                        // Add the results to the map
                        results.put(tableName, tableResults);
                    } catch (Exception e) {
                        System.err.println("Error sampling table " + tableName + ": " + e.getMessage());
                    }
                }, executorService);
                
                batchFutures.add(future);
            }
            
            // Wait for all futures in this batch to complete or timeout
            try {
                CompletableFuture<Void> allBatchFutures = CompletableFuture.allOf(
                        batchFutures.toArray(new CompletableFuture[0]));
                allBatchFutures.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("Timeout or error while processing batch: " + e.getMessage());
                // Cancel any remaining tasks in this batch
                batchFutures.forEach(future -> future.cancel(true));
            }
        }
        
        return results;
    }
    
    /**
     * Clears the sample cache to ensure fresh results on next sampling.
     */
    public void clearCache() {
        sampleCache.clear();
    }
    
    /**
     * Shuts down the executor service used for parallel sampling.
     * Should be called when the service is no longer needed.
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
     * Creates a cache key for the given sampling parameters.
     */
    private String getCacheKey(String tableName, String columnName, int sampleSize) {
        return tableName + ":" + columnName + ":" + sampleSize;
    }
    
    /**
     * Gets the most appropriate sampler for the given column based on its metadata.
     */
    private DataSampler getBestSamplerForColumn(ColumnMetadata columnMetadata) {
        // Use the factory to get the appropriate sampler
        // Default to the provided sampler if no specialized one is available
        return samplerFactory.createSampler(columnMetadata).orElse(defaultSampler);
    }
    
    /**
     * Adaptively adjusts batch size based on performance metrics from recent sampling operations.
     */
    private void adjustBatchSizeBasedOnPerformance(Map<String, SampleResult> results) {
        if (results.isEmpty()) {
            return;
        }
        
        // Calculate average execution time
        double avgExecutionTime = results.values().stream()
                .mapToLong(SampleResult::getExecutionTimeMs)
                .average()
                .orElse(0);
        
        // If average execution time is high, reduce batch size
        int currentSize = currentBatchSize.get();
        if (avgExecutionTime > 5000 && currentSize > 1) { // 5 seconds threshold
            currentBatchSize.updateAndGet(size -> Math.max(1, size - 1));
        } 
        // If average execution time is low, increase batch size up to the default
        else if (avgExecutionTime < 1000 && currentSize < DEFAULT_BATCH_SIZE) { // 1 second threshold
            currentBatchSize.updateAndGet(size -> Math.min(DEFAULT_BATCH_SIZE, size + 1));
        }
    }
    
    /**
     * Progressive sampling for very large tables.
     * Starts with a small sample and gradually increases if needed.
     * 
     * @param tableName the name of the table
     * @param columnMetadata the column to sample
     * @param targetSampleSize the desired sample size
     * @param minSampleSize the minimum sample size to start with
     * @param qualityThreshold the threshold for sample quality (0-1)
     * @return the sampling result
     */
    public SampleResult progressiveSample(
            String tableName, 
            ColumnMetadata columnMetadata, 
            int targetSampleSize,
            int minSampleSize,
            double qualityThreshold) {
        
        // Start with the minimum sample size
        int currentSampleSize = minSampleSize;
        SampleResult result = defaultSampler.sampleColumn(tableName, columnMetadata, currentSampleSize);
        
        // If we don't have statistics yet, return the initial result
        if (result.getStatistics() == null) {
            return result;
        }
        
        // Continue sampling until we reach the target size or the quality threshold
        while (currentSampleSize < targetSampleSize && 
               isSampleQualityBelowThreshold(result.getStatistics(), qualityThreshold)) {
            
            // Double the sample size for next iteration, but don't exceed target
            int nextSampleSize = Math.min(currentSampleSize * 2, targetSampleSize);
            int additionalSamples = nextSampleSize - currentSampleSize;
            
            // Get additional samples
            SampleResult additionalResult = defaultSampler.sampleColumn(
                    tableName, columnMetadata, additionalSamples);
            
            // Merge the results
            result = mergeSampleResults(result, additionalResult);
            currentSampleSize = nextSampleSize;
        }
        
        return result;
    }
    
    /**
     * Evaluates whether the sample quality is below the specified threshold.
     * Quality is determined by uniqueness ratio and value distribution.
     */
    private boolean isSampleQualityBelowThreshold(SampleStatistics statistics, double threshold) {
        // Simple implementation - just check the uniqueness ratio
        return statistics.getUniquenessRatio() < threshold;
    }
    
    /**
     * Merges two sample results, combining their samples and statistics.
     */
    private SampleResult mergeSampleResults(SampleResult result1, SampleResult result2) {
        // Create a new combined list of samples
        List<String> combinedSamples = new ArrayList<>(result1.getSamples());
        combinedSamples.addAll(result2.getSamples());
        
        // We'll need to recalculate statistics for the combined samples
        // This is a simplistic implementation - in a real solution you'd merge the statistics more efficiently
        
        return SampleResult.builder()
                .tableName(result1.getTableName())
                .columnName(result1.getColumnName())
                .samples(combinedSamples)
                .totalRowsScanned(result1.getTotalRowsScanned() + result2.getTotalRowsScanned())
                .executionTimeMs(result1.getExecutionTimeMs() + result2.getExecutionTimeMs())
                .build();
    }
}

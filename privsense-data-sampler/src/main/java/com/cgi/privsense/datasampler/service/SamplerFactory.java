package com.cgi.privsense.datasampler.service;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.service.DataSampler;

import java.util.Optional;

/**
 * Factory for creating DataSampler instances based on column characteristics.
 * Enables dynamic selection of the most appropriate sampler for different data types and contexts.
 */
public class SamplerFactory {

    private final DataSampler randomSampler;
    private final DataSampler stratifiedSampler;
    private final DataSampler parallelSampler;
    
    /**
     * Constructs a SamplerFactory with the specified samplers.
     *
     * @param randomSampler the random sampler implementation
     * @param stratifiedSampler the stratified sampler implementation
     * @param parallelSampler the parallel sampler implementation for large tables
     */
    public SamplerFactory(
            DataSampler randomSampler,
            DataSampler stratifiedSampler,
            DataSampler parallelSampler) {
        this.randomSampler = randomSampler;
        this.stratifiedSampler = stratifiedSampler;
        this.parallelSampler = parallelSampler;
    }
    
    /**
     * Creates the most appropriate sampler for the given column based on its metadata.
     * Selection logic considers data type, size, and distribution characteristics.
     *
     * @param columnMetadata the metadata for the column to sample
     * @return an Optional containing the appropriate sampler, or empty if no specialized sampler is available
     */
    public Optional<DataSampler> createSampler(ColumnMetadata columnMetadata) {
        // For very large tables with numeric/date columns, stratified sampling often provides better distribution
        if (isNumericOrDateType(columnMetadata.getDataType())) {
            return Optional.of(stratifiedSampler);
        }
        
        // For large tables, parallel sampling is more efficient
        if (isLargeTable(columnMetadata)) {
            return Optional.of(parallelSampler);
        }
        
        // Default to random sampling for most columns
        return Optional.of(randomSampler);
    }
    
    /**
     * Checks if a data type is numeric or date-based.
     *
     * @param dataType the SQL data type string
     * @return true if the data type is numeric or date-based
     */
    private boolean isNumericOrDateType(String dataType) {
        if (dataType == null) {
            return false;
        }
        
        String type = dataType.toUpperCase();
        return type.contains("INT") || 
               type.contains("FLOAT") || 
               type.contains("DOUBLE") || 
               type.contains("DECIMAL") || 
               type.contains("NUMBER") || 
               type.contains("DATE") || 
               type.contains("TIME");
    }
    
    /**
     * Determines if a column belongs to a large table based on available metadata.
     * 
     * @param columnMetadata the column metadata
     * @return true if the column appears to be part of a large table
     */    private boolean isLargeTable(ColumnMetadata columnMetadata) {
        // This is a placeholder implementation
        // In a real implementation, you would use table statistics or size estimates
        // from the database metadata to make this determination
        
        // Since ColumnMetadata doesn't have row count information,
        // we could check other properties or use database metadata APIs
        // For now, return a conservative value that won't affect most tables
        return false; // Default to assuming tables are not "large" until we implement proper size detection
    }
}

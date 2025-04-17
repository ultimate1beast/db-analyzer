package com.cgi.privsense.datasampler.strategy;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.datasampler.base.AbstractDataSampler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the random sampling strategy.
 * This sampler fetches random samples from a column using database-specific random functions.
 */
@Slf4j
public class RandomSampler extends AbstractDataSampler {
    
    private int lastSampleSize;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Constructor with required dataSource.
     *
     * @param dataSource the data source to sample from
     */
    public RandomSampler(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Gets the sample size used in the last sampling operation.
     *
     * @return the last used sample size
     */
    protected int getSampleSize() {
        return lastSampleSize;
    }
    
    /**
     * Implements the abstract method from AbstractDataSampler.
     * Performs random sampling on the specified column.
     *
     * @param tableName the name of the table
     * @param columnMetadata metadata for the column to sample
     * @param sampleSize the number of samples to retrieve
     * @return the list of sampled values
     */
    @Override
    protected List<String> performSampling(String tableName, ColumnMetadata columnMetadata, int sampleSize) {
        log.debug("Executing random sampling on {}.{} with size {}", tableName, columnMetadata.getName(), sampleSize);
        this.lastSampleSize = sampleSize;
        
        // Build query with database-specific random function
        String query = buildRandomSamplingQuery(tableName, columnMetadata.getName(), sampleSize);
        log.debug("Random sampling query: {}", query);
        
        try {
            return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString(1));
        } catch (DataAccessException e) {
            log.warn("Error executing random sampling query, falling back to basic method", e);
            return executeBasicRandomSampling(tableName, columnMetadata.getName(), sampleSize);
        }
    }
    
    /**
     * Executes a basic random sampling query that works across most database systems.
     * Used as a fallback when database-specific optimizations fail.
     *
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @param sampleSize the number of samples to retrieve
     * @return the list of sampled values
     */
    private List<String> executeBasicRandomSampling(String tableName, String columnName, int sampleSize) {
        List<String> samples = new ArrayList<>();
        
        String query = String.format("SELECT %s FROM %s ORDER BY RAND() LIMIT %d", 
                columnName, tableName, sampleSize);
                
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                samples.add(rs.getString(1));
            }
            
        } catch (SQLException e) {
            log.error("Error executing basic random sampling", e);
            throw new RuntimeException("Failed to execute basic random sampling", e);
        }
        
        return samples;
    }
    
    /**
     * Builds a database-optimized random sampling query based on the table's estimated size.
     *
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @param sampleSize the number of samples to retrieve
     * @return the optimized sampling query
     */
    private String buildRandomSamplingQuery(String tableName, String columnName, int sampleSize) {
        // This is a simplified version - in a real implementation, we would detect the database
        // type and use the appropriate random function (RAND(), RANDOM(), etc.)
        
        // For MySQL/MariaDB
        return String.format("SELECT %s FROM %s ORDER BY RAND() LIMIT %d", 
                columnName, tableName, sampleSize);
    }
}

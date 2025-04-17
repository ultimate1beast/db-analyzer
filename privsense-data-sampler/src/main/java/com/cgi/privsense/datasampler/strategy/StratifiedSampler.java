package com.cgi.privsense.datasampler.strategy;

import com.cgi.privsense.common.exception.SamplingException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.datasampler.base.AbstractDataSampler;
import com.cgi.privsense.datasampler.util.DataTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of stratified sampling strategy.
 * This sampler tries to ensure representative samples by partitioning the data
 * into strata and sampling from each stratum proportionally.
 */
@Slf4j
public class StratifiedSampler extends AbstractDataSampler {

    private int lastSampleSize;
    private int numStrata = 5; // Default number of strata
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor with required dataSource.
     *
     * @param dataSource the data source to sample from
     */
    public StratifiedSampler(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Constructor with dataSource and number of strata.
     *
     * @param dataSource the data source to sample from
     * @param numStrata the number of strata to divide the data into
     */
    public StratifiedSampler(DataSource dataSource, int numStrata) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        if (numStrata <= 0) {
            throw new IllegalArgumentException("Number of strata must be positive");
        }
        this.numStrata = numStrata;
    }

    /**
     * Gets the number of strata used for stratified sampling.
     *
     * @return the number of strata
     */
    public int getNumStrata() {
        return numStrata;
    }

    /**
     * Sets the number of strata to use for stratified sampling.
     *
     * @param numStrata the number of strata to divide the data into
     */
    public void setNumStrata(int numStrata) {
        if (numStrata <= 0) {
            throw new IllegalArgumentException("Number of strata must be positive");
        }
        this.numStrata = numStrata;
    }

    /**
     * Implements the abstract method from AbstractDataSampler.
     * Performs stratified sampling on the specified column.
     *
     * @param tableName the name of the table
     * @param columnMetadata metadata for the column to sample
     * @param sampleSize the number of samples to retrieve
     * @return the list of sampled values
     */
    @Override
    protected List<String> performSampling(String tableName, ColumnMetadata columnMetadata, int sampleSize) {
        log.debug("Executing stratified sampling on {}.{} with size {}", 
                tableName, columnMetadata.getName(), sampleSize);
        this.lastSampleSize = sampleSize;
        
        try {
            // Determine if this column is suitable for stratification
            if (!isSuitableForStratification(columnMetadata)) {
                log.debug("Column not suitable for stratification, using random sampling instead");
                return fallbackToRandomSampling(tableName, columnMetadata.getName(), sampleSize);
            }
            
            // Get column distribution statistics
            Map<String, Integer> strataInfo = analyzeColumnDistribution(tableName, columnMetadata.getName());
            
            if (strataInfo.isEmpty() || strataInfo.size() < 2) {
                log.debug("Insufficient distribution data for stratification, using random sampling");
                return fallbackToRandomSampling(tableName, columnMetadata.getName(), sampleSize);
            }
            
            // Calculate sample size for each stratum based on its proportion
            Map<String, Integer> strataSampleSizes = calculateStrataSampleSizes(strataInfo, sampleSize);
            
            // Sample from each stratum
            List<String> combinedSamples = new ArrayList<>();
            for (Map.Entry<String, Integer> stratum : strataSampleSizes.entrySet()) {
                List<String> stratumSamples = sampleFromStratum(
                        tableName, columnMetadata.getName(), stratum.getKey(), stratum.getValue());
                combinedSamples.addAll(stratumSamples);
            }
            
            return combinedSamples;
            
        } catch (Exception e) {
            log.error("Error during stratified sampling", e);
            throw new SamplingException("Failed to execute stratified sampling", e);
        }
    }
    
    /**
     * Determines if a column is suitable for stratification based on its metadata.
     *
     * @param columnMetadata the metadata for the column
     * @return true if the column is suitable for stratification
     */
    private boolean isSuitableForStratification(ColumnMetadata columnMetadata) {
        // Check if column type is suitable for stratification (numeric or date types)
        return DataTypeUtils.isNumericOrDateType(columnMetadata.getTypeName());
    }
    
    /**
     * Analyzes a column's value distribution to determine strata.
     * 
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @return a map containing strata information
     */
    private Map<String, Integer> analyzeColumnDistribution(String tableName, String columnName) throws SQLException {
        Map<String, Integer> strataInfo = new HashMap<>();
        
        // Query to get min, max and count
        String query = String.format(
                "SELECT MIN(%s) as min_val, MAX(%s) as max_val, COUNT(%s) as row_count FROM %s WHERE %s IS NOT NULL",
                columnName, columnName, columnName, tableName, columnName);
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                // Get min and max values
                String minVal = rs.getString("min_val");
                String maxVal = rs.getString("max_val");
                int rowCount = rs.getInt("row_count");
                
                if (minVal != null && maxVal != null && rowCount > 0) {
                    // For numeric columns, divide into equal width strata
                    try {
                        // Try to convert to numbers for numeric strata
                        BigDecimal min = new BigDecimal(minVal);
                        BigDecimal max = new BigDecimal(maxVal);
                        
                        // Calculate width of each stratum
                        BigDecimal width = max.subtract(min).divide(BigDecimal.valueOf(numStrata), 2, BigDecimal.ROUND_HALF_UP);
                        
                        // Create strata and estimate their sizes proportionally
                        for (int i = 0; i < numStrata; i++) {
                            BigDecimal lowerBound = min.add(width.multiply(BigDecimal.valueOf(i)));
                            BigDecimal upperBound = (i < numStrata - 1) 
                                    ? min.add(width.multiply(BigDecimal.valueOf(i + 1)))
                                    : max.add(BigDecimal.ONE); // Ensure the last stratum includes the max value
                            
                            String stratumKey = String.format("%s-%s", lowerBound.toString(), upperBound.toString());
                            
                            // Estimate stratum size - in a real implementation, we would count actual rows in each stratum
                            int estimatedSize = rowCount / numStrata;
                            strataInfo.put(stratumKey, estimatedSize);
                        }
                    } catch (NumberFormatException e) {
                        // Not a numeric column, try string-based stratification
                        log.debug("Column is not numeric, using string-based stratification");
                        return analyzeStringColumnDistribution(tableName, columnName);
                    }
                }
            }
        }
        
        return strataInfo;
    }
    
    /**
     * Analyzes distribution for string-type columns.
     *
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @return strata information for string columns
     */
    private Map<String, Integer> analyzeStringColumnDistribution(String tableName, String columnName) throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        
        // For string columns, we can group by first character or use other string-based strategies
        String query = String.format(
                "SELECT SUBSTRING(%s, 1, 1) as first_char, COUNT(*) as count FROM %s " +
                "WHERE %s IS NOT NULL GROUP BY first_char ORDER BY first_char",
                columnName, tableName, columnName);
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String firstChar = rs.getString("first_char");
                int count = rs.getInt("count");
                if (firstChar != null && !firstChar.isEmpty()) {
                    distribution.put(firstChar, count);
                }
            }
        }
        
        return distribution;
    }
    
    /**
     * Calculates how many samples to take from each stratum.
     *
     * @param strataInfo information about each stratum
     * @param totalSampleSize the total number of samples to take
     * @return map of stratum keys to sample sizes
     */
    private Map<String, Integer> calculateStrataSampleSizes(Map<String, Integer> strataInfo, int totalSampleSize) {
        Map<String, Integer> strataSampleSizes = new HashMap<>();
        
        // Calculate total population size
        int totalPopulation = strataInfo.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalPopulation == 0) {
            return strataSampleSizes;
        }
        
        // Calculate proportional sample sizes
        int allocatedSamples = 0;
        
        for (Map.Entry<String, Integer> stratum : strataInfo.entrySet()) {
            // Calculate proportional sample size for this stratum
            int stratumSampleSize = Math.round((float) stratum.getValue() / totalPopulation * totalSampleSize);
            
            // Ensure we always take at least one sample from each stratum
            stratumSampleSize = Math.max(1, stratumSampleSize);
            
            strataSampleSizes.put(stratum.getKey(), stratumSampleSize);
            allocatedSamples += stratumSampleSize;
        }
        
        // Adjust for rounding errors
        if (allocatedSamples != totalSampleSize) {
            // Find the largest stratum
            String largestStratumKey = strataInfo.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(strataInfo.keySet().iterator().next());
            
            // Adjust its sample size
            int adjustment = totalSampleSize - allocatedSamples;
            strataSampleSizes.put(largestStratumKey, strataSampleSizes.get(largestStratumKey) + adjustment);
        }
        
        return strataSampleSizes;
    }
    
    /**
     * Samples data from a specific stratum.
     *
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @param stratumKey the key identifying the stratum
     * @param sampleSize the number of samples to take from this stratum
     * @return list of sampled values from the stratum
     */
    private List<String> sampleFromStratum(String tableName, String columnName, String stratumKey, int sampleSize) {
        List<String> samples = new ArrayList<>();
        
        try {
            // Parse stratum key to get bounds
            String[] bounds = stratumKey.split("-");
            if (bounds.length == 2) {
                // For numeric ranges
                try {
                    BigDecimal lowerBound = new BigDecimal(bounds[0]);
                    BigDecimal upperBound = new BigDecimal(bounds[1]);
                    
                    String query = String.format(
                            "SELECT %s FROM %s WHERE %s >= ? AND %s < ? ORDER BY RAND() LIMIT %d",
                            columnName, tableName, columnName, columnName, sampleSize);
                    
                    samples = jdbcTemplate.query(query, 
                            (rs, rowNum) -> rs.getString(1),
                            lowerBound, upperBound);
                    
                } catch (NumberFormatException e) {
                    // Not a numeric bound, try string approach
                    return sampleFromStringStratum(tableName, columnName, stratumKey, sampleSize);
                }
            } else {
                // For string-based strata (first character, etc.)
                return sampleFromStringStratum(tableName, columnName, stratumKey, sampleSize);
            }
        } catch (Exception e) {
            log.error("Error sampling from stratum {}", stratumKey, e);
        }
        
        return samples;
    }
    
    /**
     * Samples data for string-type columns using first character.
     *
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @param firstChar the first character to match
     * @param sampleSize the number of samples to take
     * @return list of sampled values
     */
    private List<String> sampleFromStringStratum(String tableName, String columnName, String firstChar, int sampleSize) {
        if (firstChar.length() > 1) {
            firstChar = firstChar.substring(0, 1);
        }
        
        String query = String.format(
                "SELECT %s FROM %s WHERE SUBSTRING(%s, 1, 1) = ? ORDER BY RAND() LIMIT %d",
                columnName, tableName, columnName, sampleSize);
        
        try {
            return jdbcTemplate.query(query, 
                    (rs, rowNum) -> rs.getString(1),
                    firstChar);
        } catch (Exception e) {
            log.error("Error sampling from string stratum {}", firstChar, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Fallback method that uses random sampling when stratification is not possible.
     *
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @param sampleSize the number of samples to retrieve
     * @return the list of sampled values
     */
    private List<String> fallbackToRandomSampling(String tableName, String columnName, int sampleSize) {
        String query = String.format("SELECT %s FROM %s WHERE %s IS NOT NULL ORDER BY RAND() LIMIT %d",
                columnName, tableName, columnName, sampleSize);
        
        try {
            return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString(1));
        } catch (DataAccessException e) {
            log.error("Error during fallback random sampling", e);
            throw new SamplingException("Failed to execute random sampling fallback", e);
        }
    }
}

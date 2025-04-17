package com.cgi.privsense.dbscanner.metadata;

import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.exception.MetadataExtractionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.ForeignKeyMetadata;
import com.cgi.privsense.common.model.TableMetadata;
import com.cgi.privsense.common.service.MetadataExtractor;
import com.cgi.privsense.common.util.DatabaseUtils;
import com.cgi.privsense.common.util.ValidationUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Abstract base class for metadata extractors.
 * Implements common functionality and defines template methods for database-specific operations.
 */
public abstract class AbstractMetadataExtractor implements MetadataExtractor {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractMetadataExtractor.class.getName());
    
    protected final DatabaseConnectionProvider connectionProvider;
    protected final ConnectionProperties connectionProperties;
    
    /**
     * Creates a new AbstractMetadataExtractor.
     *
     * @param connectionProvider The database connection provider
     * @param connectionProperties Connection properties for the database
     */
    protected AbstractMetadataExtractor(DatabaseConnectionProvider connectionProvider, ConnectionProperties connectionProperties) {
        ValidationUtils.notNull(connectionProvider, "Connection provider cannot be null");
        ValidationUtils.notNull(connectionProperties, "Connection properties cannot be null");
        this.connectionProvider = connectionProvider;
        this.connectionProperties = connectionProperties;
    }
      @Override
    public List<TableMetadata> extractAllTablesMetadata() {
        LOGGER.info("Extracting metadata for all tables");
        DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
        return extractAllTables(dataSource);
    }
    
    @Override
    public List<TableMetadata> extractAllTables(DataSource dataSource) {
        LOGGER.info("Extracting metadata for all tables using provided DataSource");
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            List<String> tableNames = listAllTables(connection);
            List<TableMetadata> result = new ArrayList<>();
            
            for (String tableName : tableNames) {
                try {
                    TableMetadata metadata = extractTableMetadata(connection, tableName);
                    result.add(metadata);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to extract metadata for table: " + tableName, e);
                    // Continue with other tables even if one fails
                }
            }
            
            return result;
        } catch (SQLException e) {
            throw new MetadataExtractionException("Failed to extract metadata for all tables", e);
        } finally {
            connectionProvider.closeConnection(connection);
        }
    }
      @Override
    public TableMetadata extractTableMetadata(String tableName) {
        ValidationUtils.notEmpty(tableName, "Table name cannot be empty");
        LOGGER.info("Extracting metadata for table: " + tableName);
        
        DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
        return extractTableMetadata(dataSource, tableName);
    }
    
    @Override
    public TableMetadata extractTableMetadata(DataSource dataSource, String tableName) {
        ValidationUtils.notEmpty(tableName, "Table name cannot be empty");
        ValidationUtils.notNull(dataSource, "DataSource cannot be null");
        LOGGER.info("Extracting metadata for table: " + tableName);
        
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            return extractTableMetadata(connection, tableName);
        } catch (SQLException e) {
            throw new MetadataExtractionException("Failed to extract metadata for table: " + tableName, e);
        } finally {
            connectionProvider.closeConnection(connection);
        }
    }
      @Override
    public List<TableMetadata> searchTables(String pattern) {
        ValidationUtils.notEmpty(pattern, "Pattern cannot be empty");
        LOGGER.info("Extracting metadata for tables matching pattern: " + pattern);
        
        DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
        return searchTables(dataSource, pattern);
    }
    
    @Override
    public List<TableMetadata> searchTables(DataSource dataSource, String pattern) {
        ValidationUtils.notEmpty(pattern, "Pattern cannot be empty");
        ValidationUtils.notNull(dataSource, "DataSource cannot be null");
        LOGGER.info("Extracting metadata for tables matching pattern: " + pattern);
        
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            List<String> tableNames = listAllTables(connection);
            Pattern regex = Pattern.compile(pattern);
            
            List<TableMetadata> result = new ArrayList<>();
            for (String tableName : tableNames) {
                if (regex.matcher(tableName).matches()) {
                    try {
                        TableMetadata metadata = extractTableMetadata(connection, tableName);
                        result.add(metadata);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to extract metadata for table: " + tableName, e);
                        // Continue with other tables even if one fails
                    }
                }
            }
            
            return result;
        } catch (SQLException e) {
            throw new MetadataExtractionException("Failed to extract metadata for tables matching pattern: " + pattern, e);
        } finally {
            connectionProvider.closeConnection(connection);
        }
    }
    
    /**
     * Extracts metadata for a specific table.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return The table metadata
     * @throws SQLException if a database access error occurs
     */
    protected abstract TableMetadata extractTableMetadata(Connection connection, String tableName) throws SQLException;
    
    /**
     * Lists all tables in the database.
     *
     * @param connection The database connection
     * @return A list of table names
     * @throws SQLException if a database access error occurs
     */
    protected abstract List<String> listAllTables(Connection connection) throws SQLException;
    
    /**
     * Extracts column metadata for a table.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return A list of column metadata
     * @throws SQLException if a database access error occurs
     */
    protected abstract List<ColumnMetadata> extractColumnMetadata(Connection connection, String tableName) throws SQLException;
    
    /**
     * Extracts foreign key metadata for a table.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return A list of foreign key metadata
     * @throws SQLException if a database access error occurs
     */
    protected abstract List<ForeignKeyMetadata> extractForeignKeys(Connection connection, String tableName) throws SQLException;
    
    /**
     * Calculates the size of a table in bytes.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return The size of the table in bytes
     * @throws SQLException if a database access error occurs
     */
    protected abstract long calculateTableSizeInBytes(Connection connection, String tableName) throws SQLException;
    
    /**
     * Gets the row count for a table.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return The number of rows in the table
     * @throws SQLException if a database access error occurs
     */
    protected abstract long getRowCount(Connection connection, String tableName) throws SQLException;
}

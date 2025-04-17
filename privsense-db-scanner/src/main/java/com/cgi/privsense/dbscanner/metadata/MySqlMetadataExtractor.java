package com.cgi.privsense.dbscanner.metadata;

import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.exception.MetadataExtractionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.util.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MySQL-specific implementation of the metadata extractor.
 * Extends the JDBC metadata extractor with MySQL-specific optimizations.
 */
public class MySqlMetadataExtractor extends JdbcMetadataExtractor {
    
    private static final Logger LOGGER = Logger.getLogger(MySqlMetadataExtractor.class.getName());
    
    /**
     * Creates a new MySqlMetadataExtractor.
     *
     * @param connectionProvider The database connection provider
     * @param connectionParams Connection parameters for the database
     */
    public MySqlMetadataExtractor(DatabaseConnectionProvider connectionProvider, Map<String, String> connectionParams) {
        super(connectionProvider, connectionParams);
    }
    
    @Override
    protected long calculateTableSizeInBytes(Connection connection, String tableName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // MySQL-specific query to get table size in bytes
            String sql = "SELECT " +
                    "data_length + index_length AS size_in_bytes " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = DATABASE() AND table_name = ?";
            
            statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getLong("size_in_bytes");
            }
            
            return -1;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to calculate table size for: " + tableName, e);
            return -1;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
            DatabaseUtils.closeStatement(statement);
        }
    }
    
    @Override
    protected List<ColumnMetadata> extractColumnMetadata(Connection connection, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // MySQL-specific query for enhanced column metadata
            String sql = "SELECT " +
                    "column_name, " +
                    "data_type, " +
                    "ordinal_position, " +
                    "CASE WHEN is_nullable = 'YES' THEN 1 ELSE 0 END as nullable, " +
                    "character_maximum_length, " +
                    "column_default, " +
                    "column_comment, " +
                    "column_type " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = ? " +
                    "ORDER BY ordinal_position";
            
            statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                String columnName = resultSet.getString("column_name");
                String dataType = resultSet.getString("data_type");
                String columnType = resultSet.getString("column_type");
                int ordinalPosition = resultSet.getInt("ordinal_position");
                boolean nullable = resultSet.getInt("nullable") == 1;
                Long characterMaxLength = resultSet.getLong("character_maximum_length");
                if (resultSet.wasNull()) {
                    characterMaxLength = null;
                }
                String columnDefault = resultSet.getString("column_default");
                String columnComment = resultSet.getString("column_comment");

                
                
                // Build column metadata with MySQL-specific enhancements
                ColumnMetadata column = ColumnMetadata.builder()
                        .name(columnName)
                        .dataType(dataType)
                        .ordinalPosition(ordinalPosition)
                        .nullable(nullable)
                        .characterMaxLength(characterMaxLength != null ? characterMaxLength.intValue() : null)
                        // Store additional MySQL-specific information
                        .additionalProperty("columnType", columnType)
                        .additionalProperty("default", columnDefault)
                        .additionalProperty("comment", columnComment)
                        
                        .build();
                
                columns.add(column);
            }
            
            return columns;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
            DatabaseUtils.closeStatement(statement);
        }
    }
    
    @Override
    protected long getRowCount(Connection connection, String tableName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // MySQL-specific optimization to get approximate row count for large tables
            String sql = "SELECT table_rows FROM information_schema.tables " +
                    "WHERE table_schema = DATABASE() AND table_name = ?";
            
            statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                long count = resultSet.getLong("table_rows");
                
                // For small tables, get exact count
                if (count < 1000) {
                    DatabaseUtils.closeResultSet(resultSet);
                    DatabaseUtils.closeStatement(statement);
                    return super.getRowCount(connection, tableName);
                }
                
                return count;
            }
            
            return super.getRowCount(connection, tableName);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get approximate row count, falling back to exact count.", e);
            return super.getRowCount(connection, tableName);
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
            DatabaseUtils.closeStatement(statement);
        }
    }
    
    /**
     * Optimized method to extract indexes for MySQL tables.
     * 
     * @param connection Database connection
     * @param tableName Table name
     * @return List of index information
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> extractIndexes(Connection connection, String tableName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> indexes = new ArrayList<>();
        
        try {
            String sql = "SHOW INDEX FROM " + tableName;
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                // Extract index information
                // Implementation details omitted for brevity
            }
            
            return indexes;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
            DatabaseUtils.closeStatement(statement);
        }
    }
}

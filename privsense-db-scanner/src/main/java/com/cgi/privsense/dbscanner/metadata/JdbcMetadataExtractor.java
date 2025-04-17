package com.cgi.privsense.dbscanner.metadata;

import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.exception.MetadataExtractionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.ForeignKeyMetadata;
import com.cgi.privsense.common.model.TableMetadata;
import com.cgi.privsense.common.util.DatabaseUtils;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC-based implementation of the MetadataExtractor interface.
 * Uses standard JDBC DatabaseMetaData to extract information about tables and columns.
 */
public class JdbcMetadataExtractor extends AbstractMetadataExtractor {
    
    private static final Logger LOGGER = Logger.getLogger(JdbcMetadataExtractor.class.getName());
      /**
     * Creates a new JdbcMetadataExtractor.
     *
     * @param connectionProvider The database connection provider
     * @param connectionParams Connection parameters for the database
     */
    public JdbcMetadataExtractor(DatabaseConnectionProvider connectionProvider, Map<String, String> connectionParams) {
        super(connectionProvider, convertToConnectionProperties(connectionParams));
    }
    
    /**
     * Converts a Map of connection parameters to a ConnectionProperties object.
     *
     * @param connectionParams Map of connection parameters
     * @return ConnectionProperties object
     */
    private static ConnectionProperties convertToConnectionProperties(Map<String, String> connectionParams) {
        ConnectionProperties properties = new ConnectionProperties();
        
        properties.setHost(connectionParams.get("host"));
        if (connectionParams.containsKey("port")) {
            try {
                properties.setPort(Integer.parseInt(connectionParams.get("port")));
            } catch (NumberFormatException e) {
                // Use default port if parsing fails
            }
        }
        properties.setDatabase(connectionParams.get("database"));
        properties.setSchema(connectionParams.get("schema"));
        properties.setUsername(connectionParams.get("username"));
        properties.setPassword(connectionParams.get("password"));
        
        // Add any additional parameters
        for (Map.Entry<String, String> entry : connectionParams.entrySet()) {
            if (!entry.getKey().equals("host") && !entry.getKey().equals("port") && 
                !entry.getKey().equals("database") && !entry.getKey().equals("schema") &&
                !entry.getKey().equals("username") && !entry.getKey().equals("password")) {
                properties.getAdditionalParams().put(entry.getKey(), entry.getValue());
            }
        }
        
        return properties;
    }
    
    @Override
    protected List<String> listAllTables(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        ResultSet resultSet = null;
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tables.add(tableName);
            }
            
            return tables;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
        }
    }
    
    @Override
    protected TableMetadata extractTableMetadata(Connection connection, String tableName) throws SQLException {
        // Get column metadata
        List<ColumnMetadata> columns = extractColumnMetadata(connection, tableName);
        
        // Get primary key information
        Set<String> primaryKeys = extractPrimaryKeys(connection, tableName);
        
        // Mark primary key columns
        for (ColumnMetadata column : columns) {
            if (primaryKeys.contains(column.getName())) {
                column.setPrimaryKey(true);
            }
        }
        
        // Get foreign keys
        List<ForeignKeyMetadata> foreignKeys = extractForeignKeys(connection, tableName);
        
        // Get table statistics
        long rowCount = getRowCount(connection, tableName);
        long sizeInBytes = calculateTableSizeInBytes(connection, tableName);
        
        // Get schema information
        String schema = getTableSchema(connection, tableName);
        
        return TableMetadata.builder()
                .name(tableName)
                .schema(schema)
                .columns(columns)
                .primaryKeys(new ArrayList<>(primaryKeys))
                .foreignKeys(foreignKeys)
                .rowCount(rowCount)
                .sizeInBytes(sizeInBytes)
                .build();
    }
    
    @Override
    protected List<ColumnMetadata> extractColumnMetadata(Connection connection, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        ResultSet resultSet = null;
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getColumns(null, null, tableName, "%");
            
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String dataType = resultSet.getString("TYPE_NAME");
                int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
                boolean nullable = resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                int characterMaxLength = resultSet.getInt("COLUMN_SIZE");
                
                // Build column metadata
                ColumnMetadata column = ColumnMetadata.builder()
                        .name(columnName)
                        .dataType(dataType)
                        .ordinalPosition(ordinalPosition)
                        .nullable(nullable)
                        .characterMaxLength(characterMaxLength > 0 ? characterMaxLength : null)
                        .build();
                
                columns.add(column);
            }
            
            return columns;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
        }
    }
    
    /**
     * Extracts primary key information for a table.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return A set of primary key column names
     * @throws SQLException if a database access error occurs
     */
    protected Set<String> extractPrimaryKeys(Connection connection, String tableName) throws SQLException {
        Set<String> primaryKeys = new HashSet<>();
        ResultSet resultSet = null;
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getPrimaryKeys(null, null, tableName);
            
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                primaryKeys.add(columnName);
            }
            
            return primaryKeys;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
        }
    }
    
    @Override
    protected List<ForeignKeyMetadata> extractForeignKeys(Connection connection, String tableName) throws SQLException {
        List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();
        ResultSet resultSet = null;
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getImportedKeys(null, null, tableName);
            
            while (resultSet.next()) {
                String constraintName = resultSet.getString("FK_NAME");
                String columnName = resultSet.getString("FKCOLUMN_NAME");
                String referencedTable = resultSet.getString("PKTABLE_NAME");
                String referencedColumn = resultSet.getString("PKCOLUMN_NAME");
                
                ForeignKeyMetadata foreignKey = ForeignKeyMetadata.builder()
                        .constraintName(constraintName)
                        .columnName(columnName)
                        .referencedTable(referencedTable)
                        .referencedColumn(referencedColumn)
                        .build();
                
                foreignKeys.add(foreignKey);
            }
            
            return foreignKeys;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
        }
    }
    
    /**
     * Gets the schema name for a table.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return The schema name
     * @throws SQLException if a database access error occurs
     */
    protected String getTableSchema(Connection connection, String tableName) throws SQLException {
        ResultSet resultSet = null;
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            
            if (resultSet.next()) {
                return resultSet.getString("TABLE_SCHEM");
            }
            
            return null;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
        }
    }
    
    @Override
    protected long calculateTableSizeInBytes(Connection connection, String tableName) throws SQLException {
        // This is a database-specific operation, so it's left for subclasses to implement
        // Default implementation returns -1 to indicate that the size is unknown
        return -1;
    }
    
    @Override
    protected long getRowCount(Connection connection, String tableName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName;
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get row count for table: " + tableName, e);
            return -1;
        } finally {
            DatabaseUtils.closeResultSet(resultSet);
            DatabaseUtils.closeStatement(statement);
        }
    }
}

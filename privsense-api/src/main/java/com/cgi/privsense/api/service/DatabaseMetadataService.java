package com.cgi.privsense.api.service;

import com.cgi.privsense.api.dto.ConnectionRequestDto;
import com.cgi.privsense.api.dto.TableMetadataDto;
import com.cgi.privsense.api.mapper.MetadataMapper;
import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.exception.DatabaseConnectionException;
import com.cgi.privsense.common.exception.MetadataExtractionException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.TableMetadata;
import com.cgi.privsense.common.service.MetadataExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for retrieving and managing database metadata.
 * Provides methods to list tables, get table details, and search tables.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMetadataService {

    private final DatabaseConnectionProvider connectionProvider;
    private final MetadataExtractor metadataExtractor;
    private final MetadataMapper metadataMapper;

    /**
     * Lists all tables in the database.
     *
     * @param connectionRequest database connection parameters
     * @return list of table metadata DTOs
     * @throws DatabaseConnectionException if connection fails
     * @throws MetadataExtractionException if metadata extraction fails
     */
    @Cacheable(value = "tablesList", key = "#connectionRequest.databaseType + '-' + #connectionRequest.connectionParams.get('host') + '-' + #connectionRequest.connectionParams.get('database')")
    public List<TableMetadataDto> listAllTables(ConnectionRequestDto connectionRequest) {
        try {
            log.info("Listing all tables for database: {}", 
                    connectionRequest.getConnectionParams().getOrDefault("database", "unknown"));
              ConnectionProperties connectionProperties = metadataMapper.toConnectionProperties(connectionRequest);
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            List<TableMetadata> tables = metadataExtractor.extractAllTables(dataSource);
            
            log.info("Found {} tables in database", tables.size());
            return tables.stream()
                    .map(metadataMapper::toTableMetadataDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing tables: {}", e.getMessage(), e);
            throw new MetadataExtractionException("Failed to list tables", e);
        }
    }
        /**
     * Gets metadata for a specific table.
     *
     * @param tableName table name to get metadata for
     * @param connectionRequest database connection parameters
     * @return table metadata DTO
     * @throws DatabaseConnectionException if connection fails
     * @throws MetadataExtractionException if metadata extraction fails
     */
    @Cacheable(value = "tableMetadata", key = "#connectionRequest.databaseType + '-' + #connectionRequest.connectionParams.get('host') + '-' + #connectionRequest.connectionParams.get('database') + '-' + #tableName")
    public TableMetadataDto getTableMetadata(String tableName, ConnectionRequestDto connectionRequest) {
        try {
            log.info("Getting metadata for table: {} in database: {}", 
                    tableName,
                    connectionRequest.getConnectionParams().getOrDefault("database", "unknown"));
              ConnectionProperties connectionProperties = metadataMapper.toConnectionProperties(connectionRequest);
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            TableMetadata table = metadataExtractor.extractTableMetadata(dataSource, tableName);
            
            if (table == null) {
                throw new MetadataExtractionException("Table not found: " + tableName);
            }
            
            log.info("Retrieved metadata for table: {} with {} columns", 
                    table.getName(), 
                    table.getColumns() != null ? table.getColumns().size() : 0);
                    
            return metadataMapper.toTableMetadataDto(table);
        } catch (Exception e) {
            log.error("Error getting table metadata: {}", e.getMessage(), e);
            throw new MetadataExtractionException("Failed to get table metadata", e);
        }
    }

    /**
     * Searches tables by name pattern.
     *
     * @param pattern table name pattern to search for (supports SQL wildcards)
     * @param connectionRequest database connection parameters
     * @return list of matching table metadata DTOs
     * @throws DatabaseConnectionException if connection fails
     * @throws MetadataExtractionException if metadata extraction fails
     */
    public List<TableMetadataDto> searchTables(String pattern, ConnectionRequestDto connectionRequest) {
        try {
            log.info("Searching tables with pattern: {} in database: {}", 
                    pattern,
                    connectionRequest.getConnectionParams().getOrDefault("database", "unknown"));
              ConnectionProperties connectionProperties = metadataMapper.toConnectionProperties(connectionRequest);
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            List<TableMetadata> tables = metadataExtractor.searchTables(dataSource, pattern);
            
            log.info("Found {} tables matching pattern: {}", tables.size(), pattern);
            return tables.stream()
                    .map(metadataMapper::toTableMetadataDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching tables: {}", e.getMessage(), e);
            throw new MetadataExtractionException("Failed to search tables", e);
        }
    }
}

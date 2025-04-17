package com.cgi.privsense.api.mapper;

import com.cgi.privsense.api.dto.ColumnMetadataDto;
import com.cgi.privsense.api.dto.ConnectionPoolConfigDto;
import com.cgi.privsense.api.dto.ConnectionRequestDto;
import com.cgi.privsense.api.dto.ForeignKeyMetadataDto;
import com.cgi.privsense.api.dto.TableMetadataDto;
import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.database.PoolConfig;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.ForeignKeyMetadata;
import com.cgi.privsense.common.model.TableMetadata;
import org.springframework.stereotype.Component;
import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between metadata DTOs and domain models.
 */
@Component
public class MetadataMapper {    /**
     * Converts a ConnectionRequestDto to ConnectionProperties.
     *
     * @param dto the connection request DTO
     * @return connection properties for the database
     */    public ConnectionProperties toConnectionProperties(ConnectionRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        ConnectionProperties properties = new ConnectionProperties();
        properties.setDatabaseType(dto.getDatabaseType());
        
        // Extract connection details from the connectionParams Map
        Map<String, String> params = dto.getConnectionParams();
        if (params != null) {
            properties.setHost(params.getOrDefault("host", ""));
            // Convert port string to Integer, with null as fallback if conversion fails
            String portStr = params.getOrDefault("port", null);
            if (portStr != null && !portStr.isEmpty()) {
                try {
                    properties.setPort(Integer.parseInt(portStr));
                } catch (NumberFormatException e) {
                    // Log or handle the error as needed
                }
            }
            properties.setDatabase(params.getOrDefault("database", ""));
            properties.setSchema(params.getOrDefault("schema", ""));
            properties.setUsername(params.getOrDefault("username", ""));
            properties.setPassword(params.getOrDefault("password", ""));
            properties.setAdditionalParams(params);
        }
        
        if (dto.getPoolConfig() != null) {
            properties.setPoolConfig(toPoolConfig(dto.getPoolConfig()));
        }
        
        return properties;
    }
      /**
     * Converts a ConnectionPoolConfigDto to PoolConfig.
     *
     * @param dto the pool configuration DTO
     * @return pool configuration domain object
     */    public PoolConfig toPoolConfig(ConnectionPoolConfigDto dto) {
        if (dto == null) {
            return null;
        }
        
        PoolConfig config = new PoolConfig();
        config.setMaxPoolSize(dto.getMaxPoolSize());
        config.setMinIdleConnections(dto.getMinIdleConnections());
        config.setConnectionMaxLifetime(dto.getConnectionMaxLifetime());
        config.setConnectionTimeout(dto.getConnectionTimeout());
        config.setIdleTimeout(dto.getIdleTimeout());
        
        return config;
    }
    
    /**
     * Converts a TableMetadata domain object to TableMetadataDto.
     *
     * @param tableMetadata the table metadata domain object
     * @return table metadata DTO
     */
    public TableMetadataDto toTableMetadataDto(TableMetadata tableMetadata) {
        if (tableMetadata == null) {
            return null;
        }
        
        TableMetadataDto dto = new TableMetadataDto();
        dto.setName(tableMetadata.getName());
        dto.setSchema(tableMetadata.getSchema());
        dto.setRowCount(tableMetadata.getRowCount());
        dto.setSizeInBytes(tableMetadata.getSizeInBytes());
        
        // Convert columns
        if (tableMetadata.getColumns() != null) {
            List<ColumnMetadataDto> columnDtos = tableMetadata.getColumns().stream()
                    .map(this::toColumnMetadataDto)
                    .collect(Collectors.toList());
            dto.setColumns(columnDtos.toArray(new ColumnMetadataDto[0]));
        }
        
        // Convert primary keys
        if (tableMetadata.getPrimaryKeys() != null) {
            dto.setPrimaryKeys(tableMetadata.getPrimaryKeys().toArray(new String[0]));
        }
        
        // Convert foreign keys
        if (tableMetadata.getForeignKeys() != null) {
            List<ForeignKeyMetadataDto> fkDtos = tableMetadata.getForeignKeys().stream()
                    .map(this::toForeignKeyMetadataDto)
                    .collect(Collectors.toList());
            dto.setForeignKeys(fkDtos.toArray(new ForeignKeyMetadataDto[0]));
        }
        
        return dto;
    }
    
    /**
     * Converts a ColumnMetadata domain object to ColumnMetadataDto.
     *
     * @param columnMetadata the column metadata domain object
     * @return column metadata DTO
     */
    public ColumnMetadataDto toColumnMetadataDto(ColumnMetadata columnMetadata) {
        if (columnMetadata == null) {
            return null;
        }
        
        ColumnMetadataDto dto = new ColumnMetadataDto();
        dto.setName(columnMetadata.getName());
        dto.setTableName(columnMetadata.getTableName());
        dto.setDataType(columnMetadata.getDataType());
        dto.setOrdinalPosition(columnMetadata.getOrdinalPosition());
        dto.setNullable(columnMetadata.isNullable());
        dto.setCharacterMaxLength(columnMetadata.getCharacterMaxLength());
        dto.setIsPrimaryKey(columnMetadata.isPrimaryKey());
        dto.setColumnDefault(columnMetadata.getColumnDefault());
        dto.setNumericPrecision(columnMetadata.getNumericPrecision());
        dto.setNumericScale(columnMetadata.getNumericScale());
        
        return dto;
    }
    
    /**
     * Converts a ForeignKeyMetadata domain object to ForeignKeyMetadataDto.
     *
     * @param fkMetadata the foreign key metadata domain object
     * @return foreign key metadata DTO
     */
    public ForeignKeyMetadataDto toForeignKeyMetadataDto(ForeignKeyMetadata fkMetadata) {
        if (fkMetadata == null) {
            return null;
        }
        
        ForeignKeyMetadataDto dto = new ForeignKeyMetadataDto();
        dto.setConstraintName(fkMetadata.getConstraintName());
        dto.setColumnName(fkMetadata.getColumnName());
        dto.setTableName(fkMetadata.getTableName());
        dto.setReferencedTableName(fkMetadata.getReferencedTableName());
        dto.setReferencedColumnName(fkMetadata.getReferencedColumnName());
        
        return dto;
    }
}

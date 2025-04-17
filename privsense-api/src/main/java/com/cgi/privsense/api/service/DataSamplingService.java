package com.cgi.privsense.api.service;

import com.cgi.privsense.api.dto.SampleRequestDto;
import com.cgi.privsense.api.dto.SampleResultDto;
import com.cgi.privsense.api.mapper.SamplingMapper;
import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.exception.DatabaseConnectionException;
import com.cgi.privsense.common.exception.SamplingException;
import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.model.TableMetadata;
import com.cgi.privsense.common.service.DataSampler;
import com.cgi.privsense.common.service.MetadataExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for data sampling operations.
 * Provides functionality to sample column values from database tables.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSamplingService {

    private final DatabaseConnectionProvider connectionProvider;
    private final MetadataExtractor metadataExtractor;
    private final DataSampler dataSampler;
    private final SamplingMapper samplingMapper;

    /**
     * Samples data from a specific column in the database.
     *
     * @param request sampling request parameters
     * @return sample result DTO
     * @throws DatabaseConnectionException if connection fails
     * @throws SamplingException if sampling fails
     */
    @Cacheable(value = "columnSamples", 
            key = "#request.connectionRequest.databaseType + '-' + " +
                 "#request.connectionRequest.connectionParams.get('database') + '-' + " +
                 "#request.tableName + '-' + #request.columnName + '-' + #request.sampleSize",
            condition = "#request.columnName != null")
    public SampleResultDto sampleColumn(SampleRequestDto request) {
        try {
            log.info("Sampling column: {}.{}, sample size: {}", 
                    request.getTableName(), 
                    request.getColumnName(),
                    request.getSampleSize());
            
            ConnectionProperties connectionProperties = samplingMapper.toConnectionProperties(request.getConnectionRequest());
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            // Get column metadata
            TableMetadata tableMetadata = metadataExtractor.extractTableMetadata(
                    dataSource, request.getTableName());
            
            if (tableMetadata == null) {
                throw new SamplingException("Table not found: " + request.getTableName());
            }
            
            ColumnMetadata columnMetadata = tableMetadata.getColumns().stream()
                    .filter(col -> col.getName().equals(request.getColumnName()))
                    .findFirst()
                    .orElseThrow(() -> new SamplingException(
                            "Column not found: " + request.getColumnName()));
            
            // Sample the data
            SampleResult sampleResult = dataSampler.sampleColumn(
                    dataSource.toString(), 
                    columnMetadata, 
                    request.getSampleSize());
            
            log.info("Sampled {} values from column: {}.{}", 
                    sampleResult.getSamples().size(),
                    request.getTableName(),
                    request.getColumnName());
                    
            return samplingMapper.toSampleResultDto(sampleResult);
        } catch (Exception e) {
            log.error("Error sampling column data: {}", e.getMessage(), e);
            throw new SamplingException("Failed to sample column data", e);
        }
    }

    /**
     * Samples data from all columns in a table.
     *
     * @param request sampling request parameters
     * @return list of sample result DTOs for each column
     * @throws DatabaseConnectionException if connection fails
     * @throws SamplingException if sampling fails
     */
    public List<SampleResultDto> sampleTable(SampleRequestDto request) {
        try {
            log.info("Sampling table: {}, sample size: {}", 
                    request.getTableName(), 
                    request.getSampleSize());
            
            ConnectionProperties connectionProperties = samplingMapper.toConnectionProperties(request.getConnectionRequest());
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            // Get table metadata
            TableMetadata tableMetadata = metadataExtractor.extractTableMetadata(
                    dataSource, request.getTableName());
            
            if (tableMetadata == null) {
                throw new SamplingException("Table not found: " + request.getTableName());
            }
            
            // Sample data for each column
            List<SampleResult> sampleResults = new ArrayList<>();
            for (ColumnMetadata column : tableMetadata.getColumns()) {
                try {
                    SampleResult sampleResult = dataSampler.sampleColumn(
                            dataSource.toString(), 
                            column, 
                            request.getSampleSize());
                    sampleResults.add(sampleResult);
                } catch (Exception e) {
                    log.warn("Failed to sample column: {}, skipping. Error: {}", 
                            column.getName(), e.getMessage());
                }
            }
            
            log.info("Sampled {} columns from table: {}", 
                    sampleResults.size(), request.getTableName());
                    
            return sampleResults.stream()
                    .map(samplingMapper::toSampleResultDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error sampling table data: {}", e.getMessage(), e);
            throw new SamplingException("Failed to sample table data", e);
        }
    }
}

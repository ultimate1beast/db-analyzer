package com.cgi.privsense.api.service;

import com.cgi.privsense.api.dto.PiiDetectionRequestDto;
import com.cgi.privsense.api.dto.PiiDetectionResultDto;
import com.cgi.privsense.api.mapper.PiiDetectionMapper;
import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.database.DatabaseConnectionProvider;
import com.cgi.privsense.common.exception.DatabaseConnectionException;
import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.model.*;
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
 * Service for PII detection operations.
 * Provides methods to detect PII in database columns and tables.
 */
@Slf4j
@RequiredArgsConstructor
@Service("apiPiiDetectionService") 
public class PiiDetectionService {

    private final DatabaseConnectionProvider connectionProvider;
    private final MetadataExtractor metadataExtractor;
    private final DataSampler dataSampler;
    private final com.cgi.privsense.piidetector.service.PiiDetectionService piiDetectorService;
    private final PiiDetectionMapper piiDetectionMapper;

    /**
     * Detects PII in a specific database column.
     *
     * @param request PII detection request parameters
     * @return PII detection result DTO
     * @throws DatabaseConnectionException if connection fails
     * @throws PiiDetectionException if PII detection fails
     */
    @Cacheable(value = "columnPiiDetection", 
            key = "#request.connectionRequest.databaseType + '-' + " +
                 "#request.connectionRequest.connectionParams.get('database') + '-' + " +
                 "#request.tableName + '-' + #request.columnName",
            condition = "#request.columnName != null")
    public PiiDetectionResultDto detectColumnPii(PiiDetectionRequestDto request) {
        try {
            log.info("Detecting PII in column: {}.{}", 
                    request.getTableName(), 
                    request.getColumnName());
            
            if (request.getColumnName() == null || request.getColumnName().isEmpty()) {
                throw new PiiDetectionException("Column name must be specified for column PII detection");
            }
            
            ConnectionProperties connectionProperties = piiDetectionMapper.toConnectionProperties(request.getConnectionRequest());
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            // Get column metadata
            TableMetadata tableMetadata = metadataExtractor.extractTableMetadata(
                    dataSource, request.getTableName());
            
            if (tableMetadata == null) {
                throw new PiiDetectionException("Table not found: " + request.getTableName());
            }
            
            ColumnMetadata columnMetadata = tableMetadata.getColumns().stream()
                    .filter(col -> col.getName().equals(request.getColumnName()))
                    .findFirst()
                    .orElseThrow(() -> new PiiDetectionException(
                            "Column not found: " + request.getColumnName()));
            
            // Sample the data
            SampleResult sampleResult = dataSampler.sampleColumn(
                    dataSource.toString(), 
                    columnMetadata, 
                    request.getSampleSize());
            
            // Configure detection options
            configurePiiDetectionOptions(request);
            
            // Detect PII
            PiiDetectionResult result = piiDetectorService.detectColumnPii(columnMetadata, sampleResult);
            
            log.info("PII detection for column {}.{}: containsPii={}, type={}, confidence={}", 
                    request.getTableName(),
                    request.getColumnName(),
                    result.isContainsPii(),
                    result.getPiiType(),
                    result.getConfidenceScore());
                    
            return piiDetectionMapper.toPiiDetectionResultDto(result);
        } catch (Exception e) {
            log.error("Error during PII detection for column: {}", e.getMessage(), e);
            throw new PiiDetectionException("Failed to detect PII in column", e);
        }
    }

    /**
     * Detects PII in all columns of a table.
     *
     * @param request PII detection request parameters
     * @return list of PII detection result DTOs for each column
     * @throws DatabaseConnectionException if connection fails
     * @throws PiiDetectionException if PII detection fails
     */
    public List<PiiDetectionResultDto> detectTablePii(PiiDetectionRequestDto request) {
        try {
            log.info("Detecting PII in table: {}", request.getTableName());
            
            ConnectionProperties connectionProperties = piiDetectionMapper.toConnectionProperties(request.getConnectionRequest());
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            // Get table metadata
            TableMetadata tableMetadata = metadataExtractor.extractTableMetadata(
                    dataSource, request.getTableName());
            
            if (tableMetadata == null) {
                throw new PiiDetectionException("Table not found: " + request.getTableName());
            }
            
            // Configure detection options
            configurePiiDetectionOptions(request);
            
            // Get samples and detect PII for each column
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
            
            // Detect PII in all columns
            List<PiiDetectionResult> results = piiDetectorService.detectTablePii(tableMetadata, sampleResults);
            
            log.info("Completed PII detection for {} columns in table: {}", 
                    results.size(), request.getTableName());
                    
            return results.stream()
                    .map(piiDetectionMapper::toPiiDetectionResultDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error during PII detection for table: {}", e.getMessage(), e);
            throw new PiiDetectionException("Failed to detect PII in table", e);
        }
    }

    /**
     * Detects PII in multiple specified columns.
     *
     * @param request PII detection request parameters
     * @return list of PII detection result DTOs for specified columns
     * @throws DatabaseConnectionException if connection fails
     * @throws PiiDetectionException if PII detection fails
     */
    public List<PiiDetectionResultDto> detectMultipleColumnsPii(PiiDetectionRequestDto request) {
        try {
            List<String> columns = request.getColumns();
            if (columns == null || columns.isEmpty()) {
                // If no columns specified, detect PII in all columns
                return detectTablePii(request);
            }
            
            log.info("Detecting PII in {} specified columns of table: {}", 
                    columns.size(), request.getTableName());
            
            ConnectionProperties connectionProperties = piiDetectionMapper.toConnectionProperties(request.getConnectionRequest());
            DataSource dataSource = connectionProvider.getDataSource(connectionProperties);
            
            // Get table metadata
            TableMetadata tableMetadata = metadataExtractor.extractTableMetadata(
                    dataSource, request.getTableName());
            
            if (tableMetadata == null) {
                throw new PiiDetectionException("Table not found: " + request.getTableName());
            }
            
            // Configure detection options
            configurePiiDetectionOptions(request);
            
            // Filter columns by the specified list
            List<ColumnMetadata> selectedColumns = tableMetadata.getColumns().stream()
                    .filter(col -> columns.contains(col.getName()))
                    .collect(Collectors.toList());
            
            if (selectedColumns.isEmpty()) {
                throw new PiiDetectionException("None of the specified columns were found in the table");
            }
            
            // Get samples and prepare for batch detection
            List<SampleResult> sampleResults = new ArrayList<>();
            for (ColumnMetadata column : selectedColumns) {
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
            
            // Detect PII in batch
            List<PiiDetectionResult> results = piiDetectorService.detectBatchPii(
                    selectedColumns, sampleResults);
            
            log.info("Completed PII detection for {} specified columns in table: {}", 
                    results.size(), request.getTableName());
                    
            return results.stream()
                    .map(piiDetectionMapper::toPiiDetectionResultDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error during PII detection for specified columns: {}", e.getMessage(), e);
            throw new PiiDetectionException("Failed to detect PII in specified columns", e);
        }
    }
    
    /**
     * Configures PII detection options based on request parameters.
     *
     * @param request PII detection request parameters
     */
    private void configurePiiDetectionOptions(PiiDetectionRequestDto request) {
        try {
            log.debug("Configuring PII detection options: heuristic={}, regex={}, ner={}, minConfidence={}", 
                    request.isEnableHeuristicDetection(),
                    request.isEnableRegexDetection(),
                    request.isEnableNerDetection(),
                    request.getMinimumConfidence());
            
            // This would typically update settings in the PiiDetectionFacade
            // For now, we'll just log the configuration
            // In a real implementation, you would inject the PiiDetectionConfig
            // and update its properties here
        } catch (Exception e) {
            log.warn("Failed to configure PII detection options: {}", e.getMessage());
        }
    }
}

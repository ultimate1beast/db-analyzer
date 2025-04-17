package com.cgi.privsense.api.mapper;

import com.cgi.privsense.api.dto.ConnectionRequestDto;
import com.cgi.privsense.api.dto.SampleResultDto;
import com.cgi.privsense.api.dto.SampleStatisticsDto;
import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.model.SampleResult;
import com.cgi.privsense.common.model.SampleStatistics;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between sampling DTOs and domain models.
 */
@Component
public class SamplingMapper {

    private final MetadataMapper metadataMapper;

    public SamplingMapper(MetadataMapper metadataMapper) {
        this.metadataMapper = metadataMapper;
    }

    /**
     * Converts a ConnectionRequestDto to ConnectionProperties.
     *
     * @param dto the connection request DTO
     * @return connection properties for the database
     */
    public ConnectionProperties toConnectionProperties(ConnectionRequestDto dto) {
        return metadataMapper.toConnectionProperties(dto);
    }
    
    /**
     * Converts a SampleResult domain object to SampleResultDto.
     *
     * @param sampleResult the sample result domain object
     * @return sample result DTO
     */
    public SampleResultDto toSampleResultDto(SampleResult sampleResult) {
        if (sampleResult == null) {
            return null;
        }
        
        SampleResultDto dto = new SampleResultDto();
        dto.setTableName(sampleResult.getTableName());
        dto.setColumnName(sampleResult.getColumnName());
        dto.setSamples(sampleResult.getSamples());
        dto.setTotalRowsScanned(sampleResult.getTotalRowsScanned());
        dto.setExecutionTimeMs(sampleResult.getExecutionTimeMs());
        dto.setSamplingStrategy(sampleResult.getSamplingStrategy());
        
        if (sampleResult.getStatistics() != null) {
            dto.setStatistics(toSampleStatisticsDto(sampleResult.getStatistics()));
        }
        
        return dto;
    }
    
    /**
     * Converts a SampleStatistics domain object to SampleStatisticsDto.
     *
     * @param statistics the sample statistics domain object
     * @return sample statistics DTO
     */
    public SampleStatisticsDto toSampleStatisticsDto(SampleStatistics statistics) {
        if (statistics == null) {
            return null;
        }
        
        SampleStatisticsDto dto = new SampleStatisticsDto();
        dto.setDistinctValueCount(statistics.getDistinctValueCount());
        dto.setNullCount(statistics.getNullCount());
        dto.setNullPercentage(statistics.getNullPercentage());
        dto.setMinValue(statistics.getMinValue());
        dto.setMaxValue(statistics.getMaxValue());
        dto.setValueDistribution(statistics.getValueDistribution());
        dto.setAverageLength(statistics.getAverageLength());
        
        return dto;
    }
}

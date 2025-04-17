package com.cgi.privsense.api.mapper;

import com.cgi.privsense.api.dto.ConnectionRequestDto;
import com.cgi.privsense.api.dto.PiiDetectionResultDto;
import com.cgi.privsense.common.database.ConnectionProperties;
import com.cgi.privsense.common.model.PiiDetectionResult;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between PII detection DTOs and domain models.
 */
@Component
public class PiiDetectionMapper {

    private final MetadataMapper metadataMapper;

    public PiiDetectionMapper(MetadataMapper metadataMapper) {
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
     * Converts a PiiDetectionResult domain object to PiiDetectionResultDto.
     *
     * @param result the PII detection result domain object
     * @return PII detection result DTO
     */
    public PiiDetectionResultDto toPiiDetectionResultDto(PiiDetectionResult result) {
        if (result == null) {
            return null;
        }
        
        PiiDetectionResultDto dto = new PiiDetectionResultDto();
        dto.setTableName(result.getTableName());
        dto.setColumnName(result.getColumnName());
        dto.setContainsPii(result.isContainsPii());
        dto.setConfidenceScore(result.getConfidenceScore());
        dto.setPiiType(result.getPiiType());
        dto.setDetectionStrategy(result.getDetectionStrategy());
        dto.setDetectionDetails(result.getDetectionDetails());
        
        return dto;
    }
}

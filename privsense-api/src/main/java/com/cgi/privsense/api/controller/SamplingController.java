package com.cgi.privsense.api.controller;

import com.cgi.privsense.api.dto.SampleRequestDto;
import com.cgi.privsense.api.dto.SampleResultDto;
import com.cgi.privsense.api.service.DataSamplingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for data sampling operations.
 * Provides endpoints to sample data from database columns.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
@Tag(name = "Sampling", description = "Operations for sampling data from database columns")
public class SamplingController {

    private final DataSamplingService samplingService;

    /**
     * Samples data from a database column.
     *
     * @param request sampling request parameters
     * @return sample result containing sample values and statistics
     */
    @Operation(
        summary = "Sample data from a column",
        description = "Returns sample values and statistics for a specified database column",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Successful operation",
                content = @Content(schema = @Schema(implementation = SampleResultDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Table or column not found"),
            @ApiResponse(responseCode = "500", description = "Error sampling data")
        }
    )
    @PostMapping("/column")
    public ResponseEntity<SampleResultDto> sampleColumn(
            @RequestBody @Valid SampleRequestDto request) {
        
        log.info("Sampling column: {}.{} with strategy: {}, size: {}", 
                request.getTableName(), 
                request.getColumnName(),
                request.getSamplingStrategy(),
                request.getSampleSize());
                
        SampleResultDto result = samplingService.sampleColumn(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Samples data from multiple columns in a table.
     *
     * @param request sampling request parameters (column name is optional)
     * @return list of sample results for each column
     */
    @Operation(
        summary = "Sample data from multiple columns",
        description = "Returns sample values and statistics for multiple columns in a table",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Successful operation",
                content = @Content(schema = @Schema(implementation = SampleResultDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Table not found"),
            @ApiResponse(responseCode = "500", description = "Error sampling data")
        }
    )
    @PostMapping("/table")
    public ResponseEntity<java.util.List<SampleResultDto>> sampleTable(
            @RequestBody @Valid SampleRequestDto request) {
        
        log.info("Sampling table: {} with strategy: {}, size: {}", 
                request.getTableName(),
                request.getSamplingStrategy(),
                request.getSampleSize());
                
        java.util.List<SampleResultDto> results = samplingService.sampleTable(request);
        return ResponseEntity.ok(results);
    }
}

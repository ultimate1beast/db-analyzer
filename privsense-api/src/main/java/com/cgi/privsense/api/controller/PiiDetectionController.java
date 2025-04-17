package com.cgi.privsense.api.controller;

import com.cgi.privsense.api.dto.PiiDetectionRequestDto;
import com.cgi.privsense.api.dto.PiiDetectionResultDto;
import com.cgi.privsense.api.service.PiiDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for PII (Personally Identifiable Information) detection operations.
 * Provides endpoints to analyze database columns for PII.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pii")
@RequiredArgsConstructor
@Tag(name = "PII Detection", description = "Operations for detecting PII in database columns")
public class PiiDetectionController {

    private final PiiDetectionService piiDetectionService;

    /**
     * Detects PII in a specific database column.
     *
     * @param request PII detection request parameters
     * @return PII detection result
     */
    @Operation(
        summary = "Detect PII in a column",
        description = "Analyzes a database column for personally identifiable information",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Successful operation",
                content = @Content(schema = @Schema(implementation = PiiDetectionResultDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Table or column not found"),
            @ApiResponse(responseCode = "500", description = "Error during PII detection")
        }
    )
    @PostMapping("/detect/column")
    public ResponseEntity<PiiDetectionResultDto> detectColumnPii(
            @RequestBody @Valid PiiDetectionRequestDto request) {
        
        log.info("Detecting PII in column: {}.{}", 
                request.getTableName(), 
                request.getColumnName());
                
        PiiDetectionResultDto result = piiDetectionService.detectColumnPii(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Detects PII in all columns of a table.
     *
     * @param request PII detection request parameters (column name is optional)
     * @return list of PII detection results for each column
     */
    @Operation(
        summary = "Detect PII in a table",
        description = "Analyzes all columns in a database table for personally identifiable information",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Successful operation",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = PiiDetectionResultDto.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Table not found"),
            @ApiResponse(responseCode = "500", description = "Error during PII detection")
        }
    )
    @PostMapping("/detect/table")
    public ResponseEntity<List<PiiDetectionResultDto>> detectTablePii(
            @RequestBody @Valid PiiDetectionRequestDto request) {
        
        log.info("Detecting PII in table: {}", request.getTableName());
                
        List<PiiDetectionResultDto> results = piiDetectionService.detectTablePii(request);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Detects PII in specified columns.
     *
     * @param request PII detection request parameters with list of columns
     * @return list of PII detection results for specified columns
     */
    @Operation(
        summary = "Detect PII in specified columns",
        description = "Analyzes specified columns for personally identifiable information",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Successful operation",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = PiiDetectionResultDto.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Table or columns not found"),
            @ApiResponse(responseCode = "500", description = "Error during PII detection")
        }
    )
    @PostMapping("/detect/columns")
    public ResponseEntity<List<PiiDetectionResultDto>> detectMultipleColumnsPii(
            @RequestBody @Valid PiiDetectionRequestDto request) {
        
        log.info("Detecting PII in {} columns of table: {}", 
                request.getColumns() != null ? request.getColumns().size() : "all",
                request.getTableName());
                
        List<PiiDetectionResultDto> results = piiDetectionService.detectMultipleColumnsPii(request);
        return ResponseEntity.ok(results);
    }
}

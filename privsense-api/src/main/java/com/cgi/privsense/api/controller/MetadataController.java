package com.cgi.privsense.api.controller;

import com.cgi.privsense.api.dto.ConnectionRequestDto;
import com.cgi.privsense.api.dto.TableMetadataDto;
import com.cgi.privsense.api.service.DatabaseMetadataService;
import io.swagger.v3.oas.annotations.Operation;
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
 * REST controller for database metadata operations.
 * Provides endpoints to list tables, get table details, and search tables.
 */
@Slf4j
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
@Tag(name = "Metadata", description = "Database metadata operations")
public class MetadataController {

    private final DatabaseMetadataService metadataService;

    @Operation(
        summary = "List all tables",
        description = "Returns a list of all tables in the provided database connection",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TableMetadataDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid connection parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/tables")
    public ResponseEntity<List<TableMetadataDto>> listAllTables(@Valid @RequestBody ConnectionRequestDto request) {
        log.info("Received request to list all tables");
        List<TableMetadataDto> tables = metadataService.listAllTables(request);
        return ResponseEntity.ok(tables);
    }

    @Operation(
        summary = "Get table metadata",
        description = "Returns detailed metadata for a specific table",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TableMetadataDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid connection parameters"),
            @ApiResponse(responseCode = "404", description = "Table not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/tables/{tableName}")
    public ResponseEntity<TableMetadataDto> getTableMetadata(
            @PathVariable String tableName,
            @Valid @RequestBody ConnectionRequestDto request) {
        log.info("Received request for metadata of table: {}", tableName);
        TableMetadataDto table = metadataService.getTableMetadata(tableName, request);
        return ResponseEntity.ok(table);
    }

    @Operation(
        summary = "Search tables",
        description = "Searches tables by name pattern (supports SQL wildcards)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation", 
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TableMetadataDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid connection parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/tables/search")
    public ResponseEntity<List<TableMetadataDto>> searchTables(
            @RequestParam String pattern,
            @Valid @RequestBody ConnectionRequestDto request) {
        log.info("Received request to search tables with pattern: {}", pattern);
        List<TableMetadataDto> tables = metadataService.searchTables(pattern, request);
        return ResponseEntity.ok(tables);
    }
}

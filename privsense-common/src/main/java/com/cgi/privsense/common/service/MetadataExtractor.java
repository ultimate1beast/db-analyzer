package com.cgi.privsense.common.service;

import com.cgi.privsense.common.model.TableMetadata;

import java.util.List;

/**
 * Interface for extracting metadata from databases.
 * Implementations should handle different database types and connection mechanisms.
 */
public interface MetadataExtractor {

    /**
     * Extracts metadata for all tables in the database.
     *
     * @return a list of TableMetadata objects
     * @throws com.cgi.privsense.common.exception.MetadataExtractionException if metadata extraction fails
     */
    List<TableMetadata> extractAllTablesMetadata();

    /**
     * Extracts metadata for a specific table.
     *
     * @param tableName the name of the table
     * @return the TableMetadata for the specified table
     * @throws com.cgi.privsense.common.exception.MetadataExtractionException if metadata extraction fails
     */
    TableMetadata extractTableMetadata(String tableName);

    /**
     * Extracts metadata for tables matching a pattern.
     *
     * @param pattern the pattern to match table names against (database-specific)
     * @return a list of TableMetadata objects for tables matching the pattern
     * @throws com.cgi.privsense.common.exception.MetadataExtractionException if metadata extraction fails
     */
    List<TableMetadata> extractTablesMetadata(String pattern);
}

package com.cgi.privsense.common.util;

import java.util.regex.Pattern;

/**
 * Utility class for string manipulation related to database objects.
 */
public final class StringUtils {

    private StringUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Escapes special characters in SQL identifiers like table and column names.
     *
     * @param identifier the identifier to escape
     * @return the escaped identifier
     */
    public static String escapeSqlIdentifier(String identifier) {
        ValidationUtils.notNull(identifier, "SQL identifier cannot be null");
        // Most databases use double quotes for escaping identifiers
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    /**
     * Escapes characters in a string literal for SQL queries.
     *
     * @param literal the string literal to escape
     * @return the escaped string literal
     */
    public static String escapeSqlLiteral(String literal) {
        ValidationUtils.notNull(literal, "SQL literal cannot be null");
        // Replace single quotes with two single quotes for SQL string literals
        return "'" + literal.replace("'", "''") + "'";
    }

    /**
     * Checks if a string likely contains SQL injection attempts.
     *
     * @param input the input to check
     * @return true if the input might contain SQL injection, false otherwise
     */    public static boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        
        // Simple pattern to detect common SQL injection attempts
        Pattern pattern = Pattern.compile(
            "(?i)\\b(select|insert|update|delete|drop|alter|exec|union|create|where)\\b|--|;\\s*\\w+\\s*--|/\\*.*\\*/|\\bOR\\s+[\\d']+\\s*=\\s*[\\d']+",
            Pattern.CASE_INSENSITIVE
        );
        
        return pattern.matcher(input).find();
    }

    /**
     * Normalizes a database object name by removing quotes and converting to uppercase.
     *
     * @param objectName the database object name to normalize
     * @return the normalized name
     */
    public static String normalizeDbObjectName(String objectName) {
        ValidationUtils.notNull(objectName, "Database object name cannot be null");
        // Remove quotes and convert to uppercase (standard SQL behavior)
        return objectName.replace("\"", "").toUpperCase();
    }

    /**
     * Checks if a string is a valid database identifier.
     *
     * @param identifier the identifier to validate
     * @return true if the identifier is valid, false otherwise
     */
    public static boolean isValidDbIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        
        // Most databases allow alphanumeric characters, underscores and sometimes $ and #
        // First character is usually a letter, underscore, or $
        Pattern pattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$#]*$");
        return pattern.matcher(identifier).matches();
    }
}

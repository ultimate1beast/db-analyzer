package com.cgi.privsense.datasampler.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.sql.Types;

/**
 * Utility class for handling different data types in database samples.
 * Provides methods for converting, validating, and handling different SQL data types.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataTypeUtils {

    /**
     * Checks if the given SQL type is a numeric type.
     *
     * @param sqlType the SQL type code from java.sql.Types
     * @return true if the type is numeric, false otherwise
     */
    public static boolean isNumericType(int sqlType) {
        return sqlType == Types.TINYINT || 
               sqlType == Types.SMALLINT || 
               sqlType == Types.INTEGER || 
               sqlType == Types.BIGINT || 
               sqlType == Types.FLOAT || 
               sqlType == Types.REAL || 
               sqlType == Types.DOUBLE || 
               sqlType == Types.NUMERIC || 
               sqlType == Types.DECIMAL;
    }

    /**
     * Checks if the given SQL type is a string type.
     *
     * @param sqlType the SQL type code from java.sql.Types
     * @return true if the type is string, false otherwise
     */
    public static boolean isStringType(int sqlType) {
        return sqlType == Types.CHAR || 
               sqlType == Types.VARCHAR || 
               sqlType == Types.LONGVARCHAR || 
               sqlType == Types.NCHAR || 
               sqlType == Types.NVARCHAR || 
               sqlType == Types.LONGNVARCHAR || 
               sqlType == Types.CLOB || 
               sqlType == Types.NCLOB;
    }

    /**
     * Checks if the given SQL type is a date/time type.
     *
     * @param sqlType the SQL type code from java.sql.Types
     * @return true if the type is date/time, false otherwise
     */
    public static boolean isDateTimeType(int sqlType) {
        return sqlType == Types.DATE || 
               sqlType == Types.TIME || 
               sqlType == Types.TIMESTAMP || 
               sqlType == Types.TIME_WITH_TIMEZONE || 
               sqlType == Types.TIMESTAMP_WITH_TIMEZONE;
    }

    /**
     * Checks if the given SQL type is a binary type.
     *
     * @param sqlType the SQL type code from java.sql.Types
     * @return true if the type is binary, false otherwise
     */
    public static boolean isBinaryType(int sqlType) {
        return sqlType == Types.BINARY || 
               sqlType == Types.VARBINARY || 
               sqlType == Types.LONGVARBINARY || 
               sqlType == Types.BLOB;
    }

    /**
     * Gets the Java class that corresponds to a SQL type.
     *
     * @param sqlType the SQL type code from java.sql.Types
     * @return the corresponding Java class
     */
    public static Class<?> getJavaTypeForSqlType(int sqlType) {
        if (isNumericType(sqlType)) {
            switch (sqlType) {
                case Types.TINYINT:
                case Types.SMALLINT:
                    return Short.class;
                case Types.INTEGER:
                    return Integer.class;
                case Types.BIGINT:
                    return Long.class;
                case Types.FLOAT:
                case Types.REAL:
                    return Float.class;
                case Types.DOUBLE:
                    return Double.class;
                case Types.NUMERIC:
                case Types.DECIMAL:
                    return java.math.BigDecimal.class;
                default:
                    return Number.class;
            }
        } else if (isStringType(sqlType)) {
            return String.class;
        } else if (isDateTimeType(sqlType)) {
            switch (sqlType) {
                case Types.DATE:
                    return java.sql.Date.class;
                case Types.TIME:
                case Types.TIME_WITH_TIMEZONE:
                    return java.sql.Time.class;
                case Types.TIMESTAMP:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    return java.sql.Timestamp.class;
                default:
                    return java.util.Date.class;
            }
        } else if (isBinaryType(sqlType)) {
            return byte[].class;
        } else {
            return Object.class;
        }
    }

    /**
     * Determines if a value needs to be quoted in SQL queries.
     *
     * @param sqlType the SQL type code from java.sql.Types
     * @return true if the type requires quotes, false otherwise
     */
    public static boolean needsQuotes(int sqlType) {
        return isStringType(sqlType) || isDateTimeType(sqlType);
    }

    /**
     * Safely truncates a string value if it exceeds the given length.
     *
     * @param value the string value
     * @param maxLength the maximum allowed length
     * @return the truncated string
     */
    public static String truncateStringValue(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
    
    /**
     * Checks if the given SQL type name is a numeric or date-based type.
     *
     * @param dataType the SQL type name as a String
     * @return true if the type is numeric or date-based
     */
    public static boolean isNumericOrDateType(String dataType) {
        if (dataType == null) {
            return false;
        }
        
        String type = dataType.toUpperCase();
        return type.contains("INT") || 
               type.contains("FLOAT") || 
               type.contains("DOUBLE") || 
               type.contains("DECIMAL") || 
               type.contains("NUMBER") || 
               type.contains("DATE") || 
               type.contains("TIME");
    }
}

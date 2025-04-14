package com.cgi.privsense.common.util;

import com.cgi.privsense.common.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for common database operations.
 */
public final class DatabaseUtils {

    private DatabaseUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Safely closes a database connection.
     *
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DatabaseConnectionException("Error closing database connection", e);
            }
        }
    }

    /**
     * Safely closes a Statement.
     *
     * @param statement the statement to close
     */
    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // Just log, don't throw to avoid masking other exceptions
                System.err.println("Error closing statement: " + e.getMessage());
            }
        }
    }

    /**
     * Safely closes a ResultSet.
     *
     * @param resultSet the result set to close
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                // Just log, don't throw to avoid masking other exceptions
                System.err.println("Error closing result set: " + e.getMessage());
            }
        }
    }

    /**
     * Safely closes JDBC resources in the correct order (ResultSet, Statement, Connection).
     *
     * @param resultSet the result set to close
     * @param statement the statement to close
     * @param connection the connection to close
     */
    public static void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        closeResultSet(resultSet);
        closeStatement(statement);
        closeConnection(connection);
    }
    
    /**
     * Executes a SQL query safely and returns whether the operation was successful.
     *
     * @param connection the database connection
     * @param sql the SQL to execute
     * @return true if execution was successful, false otherwise
     */
    public static boolean executeSqlSafely(Connection connection, String sql) {
        ValidationUtils.notNull(connection, "Connection cannot be null");
        ValidationUtils.notEmpty(sql, "SQL statement cannot be empty");
        
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            closeStatement(statement);
        }
    }
}

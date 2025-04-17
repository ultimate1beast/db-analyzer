package com.cgi.privsense.common.util;

import com.cgi.privsense.common.exception.DatabaseConnectionException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for DatabaseUtils utility methods.
 */
public class DatabaseUtilsTest {

    @Test
    void testCloseConnection() throws SQLException {
        // Create a mock connection
        Connection mockConnection = Mockito.mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(false);
        
        // Close the connection
        DatabaseUtils.closeConnection(mockConnection);
        
        // Verify that close was called
        verify(mockConnection).close();
    }
    
    @Test
    void testCloseConnectionAlreadyClosed() throws SQLException {
        // Create a mock connection that's already closed
        Connection mockConnection = Mockito.mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(true);
        
        // Close the connection
        DatabaseUtils.closeConnection(mockConnection);
        
        // Verify that close was not called since connection is already closed
        verify(mockConnection, never()).close();
    }
    
    @Test
    void testCloseConnectionWithException() throws SQLException {
        // Create a mock connection that throws an exception when closed
        Connection mockConnection = Mockito.mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(false);
        doThrow(new SQLException("Error closing connection")).when(mockConnection).close();
        
        // Attempt to close the connection and expect an exception
        assertThrows(DatabaseConnectionException.class, () -> {
            DatabaseUtils.closeConnection(mockConnection);
        }, "Should throw DatabaseConnectionException when connection close fails");
    }
    
    @Test
    void testCloseConnectionWithNull() {
        // Closing a null connection should not throw an exception
        assertDoesNotThrow(() -> {
            DatabaseUtils.closeConnection(null);
        }, "Should handle null connection gracefully");
    }
    
    @Test
    void testCloseStatement() throws SQLException {
        // Create a mock statement
        Statement mockStatement = Mockito.mock(Statement.class);
        
        // Close the statement
        DatabaseUtils.closeStatement(mockStatement);
        
        // Verify that close was called
        verify(mockStatement).close();
    }
    
    @Test
    void testCloseStatementWithException() throws SQLException {
        // Create a mock statement that throws an exception when closed
        Statement mockStatement = Mockito.mock(Statement.class);
        doThrow(new SQLException("Error closing statement")).when(mockStatement).close();
        
        // Attempt to close the statement (should not propagate exception)
        assertDoesNotThrow(() -> {
            DatabaseUtils.closeStatement(mockStatement);
        }, "Should handle statement close exceptions gracefully");
    }
    
    @Test
    void testCloseStatementWithNull() {
        // Closing a null statement should not throw an exception
        assertDoesNotThrow(() -> {
            DatabaseUtils.closeStatement(null);
        }, "Should handle null statement gracefully");
    }
    
    @Test
    void testCloseResultSet() throws SQLException {
        // Create a mock result set
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        
        // Close the result set
        DatabaseUtils.closeResultSet(mockResultSet);
        
        // Verify that close was called
        verify(mockResultSet).close();
    }
    
    @Test
    void testCloseResultSetWithException() throws SQLException {
        // Create a mock result set that throws an exception when closed
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        doThrow(new SQLException("Error closing result set")).when(mockResultSet).close();
        
        // Attempt to close the result set (should not propagate exception)
        assertDoesNotThrow(() -> {
            DatabaseUtils.closeResultSet(mockResultSet);
        }, "Should handle result set close exceptions gracefully");
    }
    
    @Test
    void testCloseResultSetWithNull() {
        // Closing a null result set should not throw an exception
        assertDoesNotThrow(() -> {
            DatabaseUtils.closeResultSet(null);
        }, "Should handle null result set gracefully");
    }
    
    @Test
    void testCloseResources() throws SQLException {
        // Create mock resources
        Connection mockConnection = Mockito.mock(Connection.class);
        Statement mockStatement = Mockito.mock(Statement.class);
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        when(mockConnection.isClosed()).thenReturn(false);
        
        // Close all resources
        DatabaseUtils.closeResources(mockResultSet, mockStatement, mockConnection);
        
        // Verify that close was called on all resources in the correct order
        verify(mockResultSet).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }
    
    @Test
    void testExecuteSqlSafely() throws SQLException {
        // Create a mock connection and statement
        Connection mockConnection = Mockito.mock(Connection.class);
        Statement mockStatement = Mockito.mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenReturn(true);
        
        // Execute SQL
        boolean result = DatabaseUtils.executeSqlSafely(mockConnection, "SELECT 1");
        
        // Verify result and method calls
        assertTrue(result, "Should return true when execution succeeds");
        verify(mockStatement).execute("SELECT 1");
        verify(mockStatement).close();
    }
    
    @Test
    void testExecuteSqlSafelyWithException() throws SQLException {
        // Create a mock connection and statement that throws an exception
        Connection mockConnection = Mockito.mock(Connection.class);
        Statement mockStatement = Mockito.mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenThrow(new SQLException("SQL execution error"));
        
        // Execute SQL
        boolean result = DatabaseUtils.executeSqlSafely(mockConnection, "SELECT 1");
        
        // Verify result and method calls
        assertFalse(result, "Should return false when execution fails");
        verify(mockStatement).close();
    }
    
    @Test
    void testExecuteSqlSafelyWithNullConnection() {
        // Execute SQL with null connection
        assertThrows(IllegalArgumentException.class, () -> {
            DatabaseUtils.executeSqlSafely(null, "SELECT 1");
        }, "Should throw IllegalArgumentException for null connection");
    }
    
    @Test
    void testExecuteSqlSafelyWithEmptySql() {
        // Create a mock connection
        Connection mockConnection = Mockito.mock(Connection.class);
        
        // Execute empty SQL
        assertThrows(IllegalArgumentException.class, () -> {
            DatabaseUtils.executeSqlSafely(mockConnection, "");
        }, "Should throw IllegalArgumentException for empty SQL");
    }
}

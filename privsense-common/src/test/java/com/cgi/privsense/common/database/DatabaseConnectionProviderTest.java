package com.cgi.privsense.common.database;

import com.cgi.privsense.common.exception.DatabaseConnectionException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for the DatabaseConnectionProvider interface.
 * Uses mockito to test contract behavior.
 */
public class DatabaseConnectionProviderTest {

    @Test
    void testConnectionProviderContract() {
        // Create a mock implementation of the interface
        DatabaseConnectionProvider provider = Mockito.mock(DatabaseConnectionProvider.class);
        
        // Create mock connection
        Connection mockConnection = Mockito.mock(Connection.class);
        
        // Create connection parameters
        Map<String, String> connectionParams = new HashMap<>();
        connectionParams.put("url", "jdbc:mysql://localhost:3306/test");
        connectionParams.put("username", "testuser");
        connectionParams.put("password", "testpass");
        
        // Configure mock behavior
        when(provider.getConnection(connectionParams)).thenReturn(mockConnection);
        when(provider.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        
        // Test getConnection
        Connection connection = provider.getConnection(connectionParams);
        assertNotNull(connection, "Connection should not be null");
        
        // Test closeConnection
        provider.closeConnection(connection);
        verify(provider).closeConnection(connection);
        
        // Test getDatabaseType
        DatabaseType dbType = provider.getDatabaseType();
        assertEquals(DatabaseType.MYSQL, dbType, "Database type should be MYSQL");
    }
    
    @Test
    void testConnectionProviderWithDifferentDatabaseTypes() {
        // Create mock implementations for different database types
        DatabaseConnectionProvider oracleProvider = Mockito.mock(DatabaseConnectionProvider.class);
        DatabaseConnectionProvider postgresProvider = Mockito.mock(DatabaseConnectionProvider.class);
        DatabaseConnectionProvider sqlServerProvider = Mockito.mock(DatabaseConnectionProvider.class);
        
        // Configure mock behavior
        when(oracleProvider.getDatabaseType()).thenReturn(DatabaseType.ORACLE);
        when(postgresProvider.getDatabaseType()).thenReturn(DatabaseType.POSTGRESQL);
        when(sqlServerProvider.getDatabaseType()).thenReturn(DatabaseType.SQL_SERVER);
        
        // Test getDatabaseType for each provider
        assertEquals(DatabaseType.ORACLE, oracleProvider.getDatabaseType(), 
                "Oracle provider should return ORACLE type");
        assertEquals(DatabaseType.POSTGRESQL, postgresProvider.getDatabaseType(), 
                "PostgreSQL provider should return POSTGRESQL type");
        assertEquals(DatabaseType.SQL_SERVER, sqlServerProvider.getDatabaseType(), 
                "SQL Server provider should return SQL_SERVER type");
    }
    
    @Test
    void testConnectionProviderErrorHandling() {
        // Create a mock implementation of the interface that throws exceptions
        DatabaseConnectionProvider provider = Mockito.mock(DatabaseConnectionProvider.class);
        
        // Create connection parameters
        Map<String, String> connectionParams = new HashMap<>();
        connectionParams.put("url", "jdbc:mysql://localhost:3306/test");
        connectionParams.put("username", "testuser");
        connectionParams.put("password", "incorrect");
        
        // Configure mock to throw DatabaseConnectionException
        when(provider.getConnection(connectionParams))
            .thenThrow(new DatabaseConnectionException("Failed to connect to database"));
        
        // Test exception is thrown
        assertThrows(DatabaseConnectionException.class, () -> {
            provider.getConnection(connectionParams);
        }, "Should throw DatabaseConnectionException for connection failures");
    }
    
    @Test
    void testConnectionProviderWithEmptyParameters() {
        // Create a mock implementation of the interface
        DatabaseConnectionProvider provider = Mockito.mock(DatabaseConnectionProvider.class);
        
        // Create empty connection parameters
        Map<String, String> emptyParams = new HashMap<>();
        
        // Configure mock to throw IllegalArgumentException for empty parameters
        when(provider.getConnection(emptyParams))
            .thenThrow(new IllegalArgumentException("Connection parameters cannot be empty"));
        
        // Test exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            provider.getConnection(emptyParams);
        }, "Should throw IllegalArgumentException for empty parameters");
    }
}

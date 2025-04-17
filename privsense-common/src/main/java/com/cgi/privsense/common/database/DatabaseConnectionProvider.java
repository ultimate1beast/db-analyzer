
package com.cgi.privsense.common.database;

import com.cgi.privsense.common.exception.DatabaseConnectionException;
import com.cgi.privsense.common.util.DatabaseUtils; // Assurez-vous que cette classe existe

import javax.sql.DataSource;
import java.sql.Connection;


/**
 * Interface for providing database connections, primarily through a DataSource.
 * Implementations handle specific database types and connection pooling.
 */
public interface DatabaseConnectionProvider {

    /**
     * Returns a configured DataSource based on the connection properties.
     * The DataSource typically manages a connection pool configured via PoolConfig.
     * Implementations should aim to reuse DataSource instances for the same logical database connection.
     *
     * @param properties The connection properties including credentials, location, and pool config.
     * @return A DataSource instance.
     * @throws DatabaseConnectionException if the DataSource cannot be configured or initialized.
     */
    DataSource getDataSource(ConnectionProperties properties) throws DatabaseConnectionException;

    /**
     * Closes the given database connection.
     * If obtained from a pooled DataSource, this typically returns the connection to the pool.
     *
     * @param connection The connection to close. Can be null.
     */
    default void closeConnection(Connection connection) {
        // Use a utility method for safe closing
        DatabaseUtils.closeConnection(connection);
    }

    /**
     * Gets the type of database this provider connects to.
     *
     * @return The DatabaseType enum value.
     */
    DatabaseType getDatabaseType();

    /**
     * Closes the underlying connection pool(s) or any resources managed by this provider.
     * Should be called on application shutdown or when the provider is no longer needed.
     * This method should handle closing any DataSource instances created by getDataSource.
     */
    void closePool();
}
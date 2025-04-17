
package com.cgi.privsense.common.database;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration properties for database connection pooling.
 */
@Data
@NoArgsConstructor
public class PoolConfig {

    // Default pool settings
    private static final int DEFAULT_MAX_POOL_SIZE = 10;
    private static final int DEFAULT_MIN_IDLE = 2;
    private static final long DEFAULT_CONNECTION_TIMEOUT_MS = 30000; // 30 seconds
    private static final long DEFAULT_IDLE_TIMEOUT_MS = 600000; // 10 minutes
    private static final long DEFAULT_MAX_LIFETIME_MS = 1800000; // 30 minutes

    private Integer maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    private Integer minIdleConnections = DEFAULT_MIN_IDLE;
    private Long connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MS;
    private Long idleTimeout = DEFAULT_IDLE_TIMEOUT_MS;
    private Long connectionMaxLifetime = DEFAULT_MAX_LIFETIME_MS;

    // Getters that handle potential null values by returning defaults
    public int getMaxPoolSize() {
        return maxPoolSize != null ? maxPoolSize : DEFAULT_MAX_POOL_SIZE;
    }

    public int getMinIdleConnections() {
        return minIdleConnections != null ? minIdleConnections : DEFAULT_MIN_IDLE;
    }

    public long getConnectionTimeout() {
        return connectionTimeout != null ? connectionTimeout : DEFAULT_CONNECTION_TIMEOUT_MS;
    }

    public long getIdleTimeout() {
        return idleTimeout != null ? idleTimeout : DEFAULT_IDLE_TIMEOUT_MS;
    }

    public long getConnectionMaxLifetime() {
        return connectionMaxLifetime != null ? connectionMaxLifetime : DEFAULT_MAX_LIFETIME_MS;
    }
}
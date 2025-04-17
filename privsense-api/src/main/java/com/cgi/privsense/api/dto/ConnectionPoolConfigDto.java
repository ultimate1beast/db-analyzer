package com.cgi.privsense.api.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for database connection pool configuration.
 * Contains parameters for configuring connection pooling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionPoolConfigDto {

    /**
     * Maximum size of the connection pool
     */
    @Min(value = 1, message = "Maximum pool size must be at least 1")
    @Builder.Default
    private int maxPoolSize = 10;

    /**
     * Minimum idle connections to maintain in the pool
     */
    @Min(value = 0, message = "Minimum idle connections cannot be negative")
    @Builder.Default
    private int minIdleConnections = 2;

    /**
     * Maximum lifetime of a connection in milliseconds
     */
    @Min(value = 1000, message = "Connection lifetime must be at least 1000ms")
    @Builder.Default
    private long connectionMaxLifetime = 1800000; // 30 minutes

    /**
     * Maximum time to wait for a connection in milliseconds
     */
    @Min(value = 100, message = "Connection timeout must be at least 100ms")
    @Builder.Default
    private long connectionTimeout = 30000; // 30 seconds

    /**
     * Maximum time a connection can sit idle in the pool in milliseconds
     */
    @Min(value = 1000, message = "Idle timeout must be at least 1000ms")
    @Builder.Default
    private long idleTimeout = 600000; // 10 minutes
}

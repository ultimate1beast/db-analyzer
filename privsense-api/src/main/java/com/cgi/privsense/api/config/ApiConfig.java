package com.cgi.privsense.api.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for API module components.
 * Sets up caching, asynchronous execution, and other shared resources.
 */
@Configuration
@EnableCaching
@EnableAsync
public class ApiConfig {
    
    /**
     * Creates a cache manager for API responses.
     * Uses in-memory concurrent map for caching.
     * 
     * @return The cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "tablesList", 
            "tableMetadata", 
            "columnSamples",
            "piiDetection"
        );
    }
    
    /**
     * Creates a task executor for asynchronous operations.
     * This is used for operations that can be executed in parallel,
     * such as scanning multiple tables or columns.
     * 
     * @return The task executor
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("privsense-api-");
        executor.initialize();
        return executor;
    }
    
    /**
     * Creates a RestTemplate for external service calls.
     * Configures timeouts to prevent hanging on unresponsive services.
     * 
     * @param builder the RestTemplateBuilder
     * @return The configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(30))
            .build();
    }
}

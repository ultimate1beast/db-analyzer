package com.cgi.privsense.datasampler.config;

import com.cgi.privsense.common.service.DataSampler;
import com.cgi.privsense.datasampler.service.DataSamplerFactory;
import com.cgi.privsense.datasampler.service.DataSamplerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Spring configuration for the data sampler module.
 * Sets up the necessary beans for the data sampling functionality.
 */
@Configuration
@ComponentScan(basePackages = "com.cgi.privsense.datasampler")
public class DataSamplerConfig {

    /**
     * Creates the DataSamplerFactory bean.
     *
     * @param dataSource the data source to use for sampling
     * @return a configured DataSamplerFactory
     */
    @Bean
    public DataSamplerFactory dataSamplerFactory(DataSource dataSource) {
        return new DataSamplerFactory(dataSource);
    }    /**
     * Creates the DataSamplerService bean.
     *
     * @param dataSamplerFactory the factory for creating data samplers
     * @return a configured DataSamplerService
     */    @Bean
    public DataSamplerService dataSamplerService(DataSamplerFactory dataSamplerFactory) {
        // Create a default sampler
        DataSampler defaultSampler = dataSamplerFactory.createDefaultSampler();
        
        // Get the system's parallelism level for the thread pool size
        int processorCount = Runtime.getRuntime().availableProcessors();
        int parallelism = Math.max(4, processorCount);
        
        // Default timeout (5 minutes)
        long timeoutSeconds = 300;
        
        return new DataSamplerService(defaultSampler, dataSamplerFactory, parallelism, timeoutSeconds);
    }
}

package com.cgi.privsense.api.config;

import com.cgi.privsense.common.service.DataSampler;
import com.cgi.privsense.datasampler.service.DataSamplerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for data sampling functionality in the API module.
 * Provides beans related to data sampling capabilities.
 */
@Configuration
@ComponentScan(basePackages = {
    "com.cgi.privsense.datasampler.service", 
    "com.cgi.privsense.datasampler.strategy"
})
public class ApiDataSamplerConfig {
    
    /**
     * Creates a default DataSampler bean using the factory.
     * This bean will be injected into services that require data sampling functionality.
     *
     * @param factory the factory to create the sampler
     * @return the default DataSampler implementation
     */
    @Bean
    public DataSampler dataSampler(DataSamplerFactory factory) {
        return factory.createDefaultSampler();
    }
}

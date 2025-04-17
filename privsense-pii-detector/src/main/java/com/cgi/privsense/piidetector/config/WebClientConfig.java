package com.cgi.privsense.piidetector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for HTTP client used to communicate with external services.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a WebClient bean with custom configuration.
     * 
     * @return configured WebClient instance
     */
    @Bean
    public WebClient webClient() {
        // Increase buffer size to handle larger responses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();
        
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}

package com.cgi.privsense.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI documentation.
 * This class sets up the Swagger UI with API information.
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * Configures the OpenAPI documentation for the PrivSense API.
     * 
     * @return OpenAPI instance with API metadata
     */
    @Bean
    public OpenAPI privSenseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PrivSense API")
                        .description("API for database scanning and PII detection")
                        .version("v1.0.0")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}

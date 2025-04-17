package com.cgi.privsense.piidetector.heuristic;

import com.cgi.privsense.common.model.PiiType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and manages column name patterns for PII detection.
 * Reads patterns from a JSON configuration file.
 */
@Slf4j
@Component
public class ColumnPatternLoader {

    @Value("${privsense.piidetection.patterns.resource:classpath:column-patterns.json}")
    private Resource patternsResource;

    private final ObjectMapper objectMapper;

    @Getter
    private Map<String, PiiType> columnPatterns;

    public ColumnPatternLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.columnPatterns = new HashMap<>();
    }

    /**
     * Loads column patterns from the configured JSON resource.
     * Called automatically after bean initialization.
     */
    @PostConstruct
    public void loadPatterns() {
        try {
            log.info("Loading column patterns from resource: {}", patternsResource);
            
            try (InputStream is = patternsResource.getInputStream()) {
                // Read the pattern definitions from the JSON file
                Map<String, String> rawPatterns = objectMapper.readValue(is, new TypeReference<Map<String, String>>() {});
                
                // Convert string PII types to enum values
                Map<String, PiiType> parsedPatterns = new HashMap<>();
                rawPatterns.forEach((pattern, typeString) -> {
                    try {
                        PiiType piiType = PiiType.valueOf(typeString.toUpperCase());
                        parsedPatterns.put(pattern.toLowerCase(), piiType);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown PII type '{}' for pattern '{}', skipping", typeString, pattern);
                    }
                });
                
                this.columnPatterns = Collections.unmodifiableMap(parsedPatterns);
                log.info("Loaded {} column patterns for PII detection", columnPatterns.size());
            }
        } catch (IOException e) {
            log.error("Failed to load column patterns", e);
            // Use built-in default patterns if loading fails
            this.columnPatterns = getDefaultPatterns();
            log.info("Using {} default column patterns", columnPatterns.size());
        }
    }

    /**
     * Returns default column patterns to use if the configuration file cannot be loaded.
     */
    private Map<String, PiiType> getDefaultPatterns() {
        Map<String, PiiType> defaults = new HashMap<>();
        
        // Email patterns
        defaults.put("email", PiiType.EMAIL);
        defaults.put("mail", PiiType.EMAIL);
        defaults.put("email_address", PiiType.EMAIL);
        
        // Name patterns
        defaults.put("name", PiiType.NAME);
        defaults.put("first_name", PiiType.NAME);
        defaults.put("last_name", PiiType.NAME);
        defaults.put("full_name", PiiType.NAME);
        defaults.put("firstname", PiiType.NAME);
        defaults.put("lastname", PiiType.NAME);
        
        // Phone patterns
        defaults.put("phone", PiiType.PHONE);
        defaults.put("telephone", PiiType.PHONE);
        defaults.put("mobile", PiiType.PHONE);
        defaults.put("cell", PiiType.PHONE);
        defaults.put("phone_number", PiiType.PHONE);
        
        // Address patterns
        defaults.put("address", PiiType.ADDRESS);
        defaults.put("street", PiiType.ADDRESS);
        defaults.put("city", PiiType.ADDRESS);
        defaults.put("state", PiiType.ADDRESS);
        defaults.put("country", PiiType.ADDRESS);
        defaults.put("zip", PiiType.ADDRESS);
        defaults.put("postal", PiiType.ADDRESS);
        
        // SSN patterns
        defaults.put("ssn", PiiType.SSN);
        defaults.put("social_security", PiiType.SSN);
        defaults.put("social_security_number", PiiType.SSN);
        
        // Credit card patterns
        defaults.put("credit_card", PiiType.CREDIT_CARD);
        defaults.put("creditcard", PiiType.CREDIT_CARD);
        defaults.put("cc_number", PiiType.CREDIT_CARD);
        defaults.put("card_number", PiiType.CREDIT_CARD);
        
        // DOB patterns
        defaults.put("birth_date", PiiType.DATE_OF_BIRTH);
        defaults.put("birthdate", PiiType.DATE_OF_BIRTH);
        defaults.put("dob", PiiType.DATE_OF_BIRTH);
        defaults.put("date_of_birth", PiiType.DATE_OF_BIRTH);
        
        // IP address patterns
        defaults.put("ip", PiiType.IP_ADDRESS);
        defaults.put("ip_address", PiiType.IP_ADDRESS);
        
        // ID patterns
        defaults.put("driver_license", PiiType.DRIVERS_LICENSE);
        defaults.put("drivers_license", PiiType.DRIVERS_LICENSE);
        defaults.put("passport", PiiType.PASSPORT);
        defaults.put("passport_number", PiiType.PASSPORT);
        
        // Financial account patterns
        defaults.put("account_number", PiiType.FINANCIAL_ACCOUNT);
        defaults.put("bank_account", PiiType.FINANCIAL_ACCOUNT);
        defaults.put("iban", PiiType.FINANCIAL_ACCOUNT);
        
        // Medical record patterns
        defaults.put("medical_record", PiiType.MEDICAL_RECORD);
        defaults.put("patient_id", PiiType.MEDICAL_RECORD);
        defaults.put("health_id", PiiType.MEDICAL_RECORD);
        
        return Collections.unmodifiableMap(defaults);
    }
}

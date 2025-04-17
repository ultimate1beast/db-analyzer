package com.cgi.privsense.piidetector.regex;

import com.cgi.privsense.common.model.PiiType;
import lombok.Getter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Library of regex patterns for detecting various types of PII.
 * Provides methods for accessing patterns by PII type and validating matches.
 */
@Component
public class PiiPatternLibrary {

    @Getter
    private final Map<PiiType, Map<String, Pattern>> patternsByType = new EnumMap<>(PiiType.class);
    
    @PostConstruct
    public void initialize() {
        // Initialize patterns for each PII type
        initEmailPatterns();
        initPhonePatterns();
        initSsnPatterns();
        initCreditCardPatterns();
        initAddressPatterns();
        initNamePatterns();
        initDateOfBirthPatterns();
        initIpAddressPatterns();
        initDriversLicensePatterns();
        initFinancialAccountPatterns();
    }
    
    /**
     * Gets all patterns for a specific PII type.
     * 
     * @param piiType the type of PII
     * @return map of pattern name to compiled Pattern object
     */
    public Map<String, Pattern> getPatternsForType(PiiType piiType) {
        return patternsByType.getOrDefault(piiType, Collections.emptyMap());
    }
    
    /**
     * Validates if a text matches a specific pattern.
     * Additional validation beyond regex may be implemented here.
     * 
     * @param pattern the Pattern to match against
     * @param text the text to validate
     * @param piiType the type of PII
     * @return true if valid match, false otherwise
     */
    public boolean isValidMatch(Pattern pattern, String text, PiiType piiType) {
        // Skip null or empty values
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // Basic regex matching
        boolean matches = pattern.matcher(text).matches();
        
        // Additional validation specific to PII type if needed
        if (matches) {
            switch (piiType) {
                case CREDIT_CARD:
                    return validateCreditCard(text);
                case SSN:
                    return validateSsn(text);
                default:
                    return true;
            }
        }
        
        return matches;
    }

    private void initEmailPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Standard email pattern
        patterns.put("standard_email", 
                Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"));
        
        patternsByType.put(PiiType.EMAIL, Collections.unmodifiableMap(patterns));
    }
    
    private void initPhonePatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // US/Canada phone with optional country code
        patterns.put("us_phone", 
                Pattern.compile("^(?:\\+?1[-. ]?)?\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$"));
        
        // International format
        patterns.put("intl_phone", 
                Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]$"));
        
        // Generic digits only pattern (7+ digits)
        patterns.put("digits_phone", 
                Pattern.compile("^[0-9]{7,15}$"));
        
        patternsByType.put(PiiType.PHONE, Collections.unmodifiableMap(patterns));
    }
    
    private void initSsnPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // US SSN (with or without dashes)
        patterns.put("us_ssn", 
                Pattern.compile("^(?!000|666|9\\d{2})\\d{3}[- ]?(?!00)\\d{2}[- ]?(?!0000)\\d{4}$"));
        
        // Canadian SIN
        patterns.put("canadian_sin", 
                Pattern.compile("^\\d{3}[- ]?\\d{3}[- ]?\\d{3}$"));
        
        patternsByType.put(PiiType.SSN, Collections.unmodifiableMap(patterns));
    }
    
    private void initCreditCardPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Generic credit card pattern (loose match)
        patterns.put("generic_cc", 
                Pattern.compile("^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9][0-9])[0-9]{12})$"));
        
        // With spaces or dashes
        patterns.put("formatted_cc", 
                Pattern.compile("^(?:4[0-9]{3}[- ]?[0-9]{4}[- ]?[0-9]{4}[- ]?[0-9]{4}|5[1-5][0-9]{2}[- ]?[0-9]{4}[- ]?[0-9]{4}[- ]?[0-9]{4})$"));
        
        patternsByType.put(PiiType.CREDIT_CARD, Collections.unmodifiableMap(patterns));
    }
    
    private void initAddressPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // US Street Address (simplified)
        patterns.put("us_street", 
                Pattern.compile("^\\d+\\s+[a-zA-Z0-9\\s,.'-]+$"));
        
        // ZIP Code
        patterns.put("us_zip", 
                Pattern.compile("^\\d{5}(?:-\\d{4})?$"));
        
        // UK Postal Code
        patterns.put("uk_postcode", 
                Pattern.compile("^[A-Z]{1,2}[0-9][A-Z0-9]? ?[0-9][A-Z]{2}$"));
        
        patternsByType.put(PiiType.ADDRESS, Collections.unmodifiableMap(patterns));
    }
    
    private void initNamePatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Simple name pattern (allows common name characters)
        patterns.put("simple_name", 
                Pattern.compile("^[\\p{L} .'-]+$"));
        
        patternsByType.put(PiiType.NAME, Collections.unmodifiableMap(patterns));
    }
    
    private void initDateOfBirthPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // US date format MM/DD/YYYY
        patterns.put("us_date", 
                Pattern.compile("^(0?[1-9]|1[012])[\\/\\-](0?[1-9]|[12][0-9]|3[01])[\\/\\-]\\d{4}$"));
        
        // ISO date YYYY-MM-DD
        patterns.put("iso_date", 
                Pattern.compile("^\\d{4}[\\/\\-](0?[1-9]|1[012])[\\/\\-](0?[1-9]|[12][0-9]|3[01])$"));
        
        patternsByType.put(PiiType.DATE_OF_BIRTH, Collections.unmodifiableMap(patterns));
    }
    
    private void initIpAddressPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // IPv4
        patterns.put("ipv4", 
                Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
        
        // IPv6
        patterns.put("ipv6", 
                Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"));
        
        patternsByType.put(PiiType.IP_ADDRESS, Collections.unmodifiableMap(patterns));
    }
    
    private void initDriversLicensePatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Generic alphanumeric pattern (common across many states)
        patterns.put("generic_dl", 
                Pattern.compile("^[A-Z0-9]{6,14}$"));
        
        patternsByType.put(PiiType.DRIVERS_LICENSE, Collections.unmodifiableMap(patterns));
    }
    
    private void initFinancialAccountPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Bank account (simplified)
        patterns.put("bank_account", 
                Pattern.compile("^\\d{4,17}$"));
        
        // IBAN (simplified)
        patterns.put("iban", 
                Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{4}\\d{7}([A-Z0-9]?){0,16}$"));
        
        patternsByType.put(PiiType.FINANCIAL_ACCOUNT, Collections.unmodifiableMap(patterns));
    }
    
    private boolean validateCreditCard(String text) {
        // Remove any non-digit characters
        String digits = text.replaceAll("\\D", "");
        
        // Check length
        if (digits.length() < 13 || digits.length() > 19) {
            return false;
        }
        
        // Luhn algorithm validation
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(digits.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
    
    private boolean validateSsn(String text) {
        // Remove any non-digit characters
        String digits = text.replaceAll("\\D", "");
        
        // Validate length
        if (digits.length() != 9) {
            return false;
        }
        
        // Check for known invalid patterns
        return !digits.equals("000000000") && 
               !digits.startsWith("000") && 
               !digits.startsWith("666") && 
               !digits.startsWith("9");
    }
}

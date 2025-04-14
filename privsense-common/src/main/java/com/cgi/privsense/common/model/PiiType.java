package com.cgi.privsense.common.model;

/**
 * Enum representing different types of Personally Identifiable Information (PII).
 * Used to categorize detected PII data in database columns.
 */
public enum PiiType {
    /**
     * Person's name or part of name
     */
    NAME,
    
    /**
     * Email address
     */
    EMAIL,
    
    /**
     * Phone number in any format
     */
    PHONE,
    
    /**
     * Physical address or parts of address
     */
    ADDRESS,
    
    /**
     * Social Security Number
     */
    SSN,
    
    /**
     * Credit card number
     */
    CREDIT_CARD,
    
    /**
     * Date of birth
     */
    DATE_OF_BIRTH,
    
    /**
     * IP address (v4 or v6)
     */
    IP_ADDRESS,
    
    /**
     * Driver's license number
     */
    DRIVERS_LICENSE,
    
    /**
     * Passport number
     */
    PASSPORT,
    
    /**
     * Financial account information
     */
    FINANCIAL_ACCOUNT,
    
    /**
     * Medical record number
     */
    MEDICAL_RECORD,
    
    /**
     * Generic ID number not falling into other categories
     */
    ID_NUMBER,
    
    /**
     * Other unspecified PII
     */
    OTHER,
    
    /**
     * Unknown PII type
     */
    UNKNOWN
}

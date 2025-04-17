package com.cgi.privsense.api.config;

import com.cgi.privsense.common.exception.DatabaseConnectionException;
import com.cgi.privsense.common.exception.MetadataExtractionException;
import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.exception.SamplingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API controllers.
 * Ensures that all exceptions are returned in a consistent format.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex validation exception
     * @return response entity with validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("message", "Invalid request parameters");
        response.put("details", errors);
        
        log.warn("Validation error: {}", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
      /**
     * Handles database connection exceptions.
     *
     * @param ex connection exception
     * @return response entity with connection error details
     */
    @ExceptionHandler(com.cgi.privsense.common.exception.DatabaseConnectionException.class)
    public ResponseEntity<Object> handleDatabaseConnectionException(com.cgi.privsense.common.exception.DatabaseConnectionException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Database Connection Error");
        response.put("message", ex.getMessage());
        
        log.error("Database connection error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * Handles metadata extraction exceptions.
     *
     * @param ex metadata exception
     * @return response entity with metadata error details
     */
    @ExceptionHandler(MetadataExtractionException.class)
    public ResponseEntity<Object> handleMetadataExtractionException(MetadataExtractionException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Metadata Extraction Error");
        response.put("message", ex.getMessage());
        
        log.error("Metadata extraction error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handles data sampling exceptions.
     *
     * @param ex sampling exception
     * @return response entity with sampling error details
     */
    @ExceptionHandler(SamplingException.class)
    public ResponseEntity<Object> handleSamplingException(SamplingException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Data Sampling Error");
        response.put("message", ex.getMessage());
        
        log.error("Data sampling error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handles PII detection exceptions.
     *
     * @param ex PII detection exception
     * @return response entity with PII detection error details
     */
    @ExceptionHandler(PiiDetectionException.class)
    public ResponseEntity<Object> handlePiiDetectionException(PiiDetectionException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "PII Detection Error");
        response.put("message", ex.getMessage());
        
        log.error("PII detection error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex general exception
     * @return response entity with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

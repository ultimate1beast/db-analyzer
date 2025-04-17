package com.cgi.privsense.api.controller;

import com.cgi.privsense.common.exception.DatabaseConnectionException;
import com.cgi.privsense.common.exception.MetadataExtractionException;
import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.exception.SamplingException;
import com.cgi.privsense.common.exception.PrivSenseException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the PrivSense API.
 * This controller advice handles exceptions thrown across all controllers
 * and provides consistent error responses.
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    /**
     * Handles generic PrivSenseException and its subclasses.
     * 
     * @param ex The exception thrown
     * @param request The web request during which the exception was thrown
     * @return ResponseEntity with error details and appropriate status
     */
    @ExceptionHandler(PrivSenseException.class)
    public ResponseEntity<ErrorResponse> handlePrivSenseException(PrivSenseException ex, WebRequest request) {
        log.error("PrivSense exception occurred: {}", ex.getMessage(), ex);
        
        HttpStatus status = determineHttpStatus(ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles validation exceptions from request body validation.
     * 
     * @param ex The validation exception
     * @param request The web request during which the exception was thrown
     * @return ResponseEntity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("Validation exception occurred: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors.toString(),
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles constraint violation exceptions (for path variable validation, etc.)
     * 
     * @param ex The constraint violation exception
     * @param request The web request during which the exception was thrown
     * @return ResponseEntity with validation error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        log.error("Constraint violation exception occurred: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> 
            errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors.toString(),
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Fallback handler for any unhandled exceptions.
     * 
     * @param ex The exception thrown
     * @param request The web request during which the exception was thrown
     * @return ResponseEntity with generic error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Determines the appropriate HTTP status code based on the exception type.
     * 
     * @param ex The PrivSenseException
     * @return The HTTP status to use in the response
     */
    private HttpStatus determineHttpStatus(PrivSenseException ex) {
        if (ex instanceof DatabaseConnectionException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof MetadataExtractionException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (ex instanceof SamplingException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (ex instanceof PiiDetectionException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * Error response data structure.
     */
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String message;
        private String details;
        private LocalDateTime timestamp;
    }
}

package com.cgi.privsense.common.exception;

/**
 * Exception thrown when there is a problem during PII detection.
 */
public class PiiDetectionException extends PrivSenseException {

    /**
     * Constructs a new PiiDetectionException with null as its detail message.
     */
    public PiiDetectionException() {
        super();
    }

    /**
     * Constructs a new PiiDetectionException with the specified detail message.
     *
     * @param message the detail message
     */
    public PiiDetectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new PiiDetectionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public PiiDetectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new PiiDetectionException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public PiiDetectionException(Throwable cause) {
        super(cause);
    }
}

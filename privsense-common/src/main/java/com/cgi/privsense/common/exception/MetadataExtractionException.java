package com.cgi.privsense.common.exception;

/**
 * Exception thrown when there is a problem extracting metadata from a database.
 */
public class MetadataExtractionException extends PrivSenseException {

    /**
     * Constructs a new MetadataExtractionException with null as its detail message.
     */
    public MetadataExtractionException() {
        super();
    }

    /**
     * Constructs a new MetadataExtractionException with the specified detail message.
     *
     * @param message the detail message
     */
    public MetadataExtractionException(String message) {
        super(message);
    }

    /**
     * Constructs a new MetadataExtractionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public MetadataExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new MetadataExtractionException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public MetadataExtractionException(Throwable cause) {
        super(cause);
    }
}

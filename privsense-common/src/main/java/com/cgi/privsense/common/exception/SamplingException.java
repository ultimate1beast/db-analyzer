package com.cgi.privsense.common.exception;

/**
 * Exception thrown when there is a problem sampling data from a database.
 */
public class SamplingException extends PrivSenseException {

    /**
     * Constructs a new SamplingException with null as its detail message.
     */
    public SamplingException() {
        super();
    }

    /**
     * Constructs a new SamplingException with the specified detail message.
     *
     * @param message the detail message
     */
    public SamplingException(String message) {
        super(message);
    }

    /**
     * Constructs a new SamplingException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public SamplingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new SamplingException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public SamplingException(Throwable cause) {
        super(cause);
    }
}

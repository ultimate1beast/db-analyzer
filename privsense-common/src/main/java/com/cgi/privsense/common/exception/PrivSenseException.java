package com.cgi.privsense.common.exception;

/**
 * Base exception class for all exceptions in the PrivSense system.
 * Extends RuntimeException for unchecked exception handling.
 */
public class PrivSenseException extends RuntimeException {

    /**
     * Constructs a new PrivSenseException with null as its detail message.
     */
    public PrivSenseException() {
        super();
    }

    /**
     * Constructs a new PrivSenseException with the specified detail message.
     *
     * @param message the detail message
     */
    public PrivSenseException(String message) {
        super(message);
    }

    /**
     * Constructs a new PrivSenseException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public PrivSenseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new PrivSenseException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public PrivSenseException(Throwable cause) {
        super(cause);
    }
}

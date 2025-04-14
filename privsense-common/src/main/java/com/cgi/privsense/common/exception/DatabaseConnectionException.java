package com.cgi.privsense.common.exception;

/**
 * Exception thrown when there is a problem connecting to a database.
 */
public class DatabaseConnectionException extends PrivSenseException {

    /**
     * Constructs a new DatabaseConnectionException with null as its detail message.
     */
    public DatabaseConnectionException() {
        super();
    }

    /**
     * Constructs a new DatabaseConnectionException with the specified detail message.
     *
     * @param message the detail message
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new DatabaseConnectionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DatabaseConnectionException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public DatabaseConnectionException(Throwable cause) {
        super(cause);
    }
}

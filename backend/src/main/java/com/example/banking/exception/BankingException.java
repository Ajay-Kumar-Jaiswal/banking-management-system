package com.example.banking.exception;

/**
 * Base class for all application-specific (business) exceptions.
 * Each subclass carries an error code that the GlobalExceptionHandler
 * maps to a specific HTTP status, keeping that mapping decision out of
 * the service layer (Single Responsibility Principle).
 */
public abstract class BankingException extends RuntimeException {

    private final String errorCode;

    protected BankingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

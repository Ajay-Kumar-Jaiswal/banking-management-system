package com.example.banking.exception;

public class InvalidCredentialsException extends BankingException {
    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message);
    }
}

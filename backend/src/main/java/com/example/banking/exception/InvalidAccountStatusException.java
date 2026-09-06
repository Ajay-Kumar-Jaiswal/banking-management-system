package com.example.banking.exception;

public class InvalidAccountStatusException extends BankingException {
    public InvalidAccountStatusException(String message) {
        super("INVALID_ACCOUNT_STATUS", message);
    }
}

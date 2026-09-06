package com.example.banking.exception;

public class DuplicateEmailException extends BankingException {
    public DuplicateEmailException(String message) {
        super("DUPLICATE_EMAIL", message);
    }
}

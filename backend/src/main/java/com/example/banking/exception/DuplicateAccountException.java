package com.example.banking.exception;

public class DuplicateAccountException extends BankingException {
    public DuplicateAccountException(String message) {
        super("DUPLICATE_ACCOUNT", message);
    }
}

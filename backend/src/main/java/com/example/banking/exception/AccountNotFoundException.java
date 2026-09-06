package com.example.banking.exception;

public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String message) {
        super("ACCOUNT_NOT_FOUND", message);
    }
}

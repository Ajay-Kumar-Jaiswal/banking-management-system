package com.example.banking.exception;

public class UserNotFoundException extends BankingException {
    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }
}

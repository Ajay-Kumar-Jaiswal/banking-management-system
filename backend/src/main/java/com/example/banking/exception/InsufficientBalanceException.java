package com.example.banking.exception;

public class InsufficientBalanceException extends BankingException {
    public InsufficientBalanceException(String message) {
        super("INSUFFICIENT_BALANCE", message);
    }
}

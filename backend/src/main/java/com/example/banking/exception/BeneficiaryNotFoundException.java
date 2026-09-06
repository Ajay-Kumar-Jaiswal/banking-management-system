package com.example.banking.exception;

public class BeneficiaryNotFoundException extends BankingException {
    public BeneficiaryNotFoundException(String message) {
        super("BENEFICIARY_NOT_FOUND", message);
    }
}

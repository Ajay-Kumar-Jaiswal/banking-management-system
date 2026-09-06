package com.example.banking.exception;

/**
 * Thrown when a logged-in customer tries to act on an account or resource
 * that does not belong to them (e.g. depositing into someone else's account).
 * Mapped to HTTP 403 FORBIDDEN, not 404, so we don't leak whether the
 * resource exists - we simply say "you may not access this".
 */
public class UnauthorizedAccountAccessException extends BankingException {
    public UnauthorizedAccountAccessException(String message) {
        super("UNAUTHORIZED_ACCOUNT_ACCESS", message);
    }
}

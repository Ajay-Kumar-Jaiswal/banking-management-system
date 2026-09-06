package com.example.banking.exception;

import com.example.banking.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Centralized exception handling. Every error response returned by this API
 * follows the same JSON shape:
 *
 * {
 *   "status": 400,
 *   "error": "INSUFFICIENT_BALANCE",
 *   "message": "Insufficient balance for this withdrawal.",
 *   "timestamp": "2026-01-01T10:15:30"
 * }
 *
 * This keeps controllers and services free of try/catch boilerplate -
 * they simply throw the right exception and this class decides the HTTP
 * status and response body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(BeneficiaryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBeneficiaryNotFound(BeneficiaryNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidAccountStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAccountStatus(InvalidAccountStatusException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransaction(InvalidTransactionException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAccount(DuplicateAccountException ex) {
        return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedAccountAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccountAccess(UnauthorizedAccountAccessException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password.", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission to perform this action.", null);
    }

    /** Handles @Valid failures on @RequestBody DTOs, collecting every field error. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "One or more fields are invalid.", details);
    }

    /** Catch-all safety net so unexpected errors never leak stack traces to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String errorCode, String message, List<String> details) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}

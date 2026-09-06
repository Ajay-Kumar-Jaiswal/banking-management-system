package com.example.banking.controller;

import com.example.banking.dto.DepositRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.WithdrawRequest;
import com.example.banking.security.CustomUserDetails;
import com.example.banking.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@AuthenticationPrincipal CustomUserDetails principal,
                                                         @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(transactionService.deposit(principal.getUserId(), request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@AuthenticationPrincipal CustomUserDetails principal,
                                                          @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(transactionService.withdraw(principal.getUserId(), request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@AuthenticationPrincipal CustomUserDetails principal,
                                                          @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transferMoney(principal.getUserId(), request));
    }

    /**
     * Lists transactions for a given account (required), optionally filtered
     * by type or by a createdAt date range. Example:
     * GET /api/transactions?accountId=1&type=DEPOSIT
     * GET /api/transactions?accountId=1&from=2026-01-01T00:00:00&to=2026-01-31T23:59:59
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam Long accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                transactionService.getTransactionsForAccount(accountId, principal.getUserId(), type, from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionDetails(@AuthenticationPrincipal CustomUserDetails principal,
                                                                      @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionDetails(id, principal.getUserId()));
    }
}

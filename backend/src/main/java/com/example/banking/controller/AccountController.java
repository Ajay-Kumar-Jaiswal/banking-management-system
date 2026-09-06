package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.security.CustomUserDetails;
import com.example.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All endpoints here require authentication (enforced globally in
 * SecurityConfig). The current user's id is pulled from the JWT-derived
 * CustomUserDetails rather than trusted from the request body/path - this
 * is what prevents a customer from spoofing another customer's id to view
 * or modify their account.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@AuthenticationPrincipal CustomUserDetails principal,
                                                           @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(accountService.getAccountsForUser(principal.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountDetails(@AuthenticationPrincipal CustomUserDetails principal,
                                                               @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountDetails(id, principal.getUserId()));
    }
}

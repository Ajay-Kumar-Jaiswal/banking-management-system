package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.UpdateAccountStatusRequest;
import com.example.banking.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints. Enforced twice, defense-in-depth style:
 * 1. SecurityConfig maps "/api/admin/**" to hasRole("ADMIN") globally.
 * 2. @PreAuthorize on each method as an explicit, self-documenting guard
 *    that survives even if the URL pattern in SecurityConfig ever changes.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getAllCustomers(search));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getAllAccounts(search));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(adminService.getAllTransactions());
    }

    @PutMapping("/accounts/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateAccountStatusRequest request) {
        return ResponseEntity.ok(adminService.updateAccountStatus(id, request));
    }
}

package com.example.banking.service;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.UpdateAccountStatusRequest;
import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.User;
import com.example.banking.enums.AccountStatus;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InvalidTransactionException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-mostly operations restricted to ADMIN users (enforced at the
 * controller/security layer via hasRole("ADMIN"), so this service assumes
 * it is only ever invoked by an authorized admin).
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public List<CustomerResponse> getAllCustomers(String search) {
        return userRepository.findAll().stream()
                .filter(u -> matches(search, u.getFullName(), u.getEmail()))
                .map(this::toCustomerResponse)
                .toList();
    }

    public List<AccountResponse> getAllAccounts(String search) {
        return accountRepository.findAll().stream()
                .filter(a -> matches(search, a.getAccountNumber(), a.getUser().getFullName()))
                .map(accountService::toResponse)
                .toList();
    }

    /** Simple case-insensitive "contains" search across a couple of fields - enough for an admin table filter. */
    private boolean matches(String search, String... fields) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase();
        for (String field : fields) {
            if (field != null && field.toLowerCase().contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Transactional
    public AccountResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));

        AccountStatus newStatus = parseStatus(request.getStatus());
        account.setStatus(newStatus);
        Account saved = accountRepository.save(account);

        return accountService.toResponse(saved);
    }

    private AccountStatus parseStatus(String raw) {
        try {
            return AccountStatus.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            throw new InvalidTransactionException("Status must be one of ACTIVE, SUSPENDED, CLOSED.");
        }
    }

    private CustomerResponse toCustomerResponse(User user) {
        return CustomerResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .accountId(transaction.getAccount().getId())
                .accountNumber(transaction.getAccount().getAccountNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().name())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

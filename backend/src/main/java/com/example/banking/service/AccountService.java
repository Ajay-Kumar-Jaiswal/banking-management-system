package com.example.banking.service;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.Account;
import com.example.banking.entity.User;
import com.example.banking.enums.AccountStatus;
import com.example.banking.enums.AccountType;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.DuplicateAccountException;
import com.example.banking.exception.InvalidTransactionException;
import com.example.banking.exception.UnauthorizedAccountAccessException;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business logic for account creation and retrieval, plus the shared
 * "ownership check" used by every other service that touches an account.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        AccountType accountType = parseAccountType(request.getAccountType());

        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsForUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountDetails(Long accountId, Long requestingUserId) {
        Account account = getAccountOrThrow(accountId);
        assertOwnership(account, requestingUserId);
        return toResponse(account);
    }

    /**
     * Fetches an account and verifies it belongs to the requesting user.
     * Used by TransactionService and BeneficiaryService before any
     * balance-affecting operation - this is the core of "customers cannot
     * touch another customer's accounts".
     */
    public Account getOwnedAccountOrThrow(Long accountId, Long requestingUserId) {
        Account account = getAccountOrThrow(accountId);
        assertOwnership(account, requestingUserId);
        return account;
    }

    public Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }

    public Account getAccountByNumberOrThrow(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));
    }

    private void assertOwnership(Account account, Long requestingUserId) {
        if (!account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccountAccessException("You do not have permission to access this account.");
        }
    }

    private String generateUniqueAccountNumber() {
        String candidate;
        int attempts = 0;
        do {
            candidate = AccountNumberGenerator.generate();
            attempts++;
            if (attempts > 10) {
                throw new DuplicateAccountException("Could not generate a unique account number, please retry.");
            }
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    private AccountType parseAccountType(String raw) {
        try {
            return AccountType.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            throw new InvalidTransactionException("Account type must be either SAVINGS or CURRENT.");
        }
    }

    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getUser().getId())
                .customerName(account.getUser().getFullName())
                .accountType(account.getAccountType().name())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();
    }
}

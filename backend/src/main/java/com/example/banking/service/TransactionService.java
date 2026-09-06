package com.example.banking.service;

import com.example.banking.dto.DepositRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.WithdrawRequest;
import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.enums.AccountStatus;
import com.example.banking.enums.TransactionStatus;
import com.example.banking.enums.TransactionType;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.exception.InvalidAccountStatusException;
import com.example.banking.exception.InvalidTransactionException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.util.TransactionReferenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The heart of the banking domain: deposit, withdrawal, and fund transfer.
 *
 * Every balance-mutating method here is @Transactional. In Spring, that
 * means the method runs inside a single database transaction: either every
 * statement inside it commits, or - if any exception is thrown - the entire
 * set of changes is rolled back as if nothing happened. This is what
 * guarantees the classic banking invariant "money is never created or
 * destroyed": in transferMoney(), if the credit to the receiver fails after
 * the sender has already been debited, the rollback undoes the debit too.
 *
 * ACID properties this demonstrates:
 * - Atomicity: transfer is all-or-nothing (the @Transactional boundary).
 * - Consistency: balances always reflect the sum of applied transactions.
 * - Isolation: concurrent transactions don't see each other's uncommitted
 *   writes (default READ_COMMITTED in MySQL/InnoDB), and our @Version
 *   optimistic-locking column on Account additionally protects against
 *   lost updates when two requests try to modify the same account's
 *   balance at nearly the same instant.
 * - Durability: once committed, changes survive even a crash immediately
 *   after (guaranteed by the underlying InnoDB storage engine).
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional
    public TransactionResponse deposit(Long requestingUserId, DepositRequest request) {
        Account account = accountService.getOwnedAccountOrThrow(request.getAccountId(), requestingUserId);
        assertActive(account);

        BigDecimal amount = validateAmount(request.getAmount());

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionReference(TransactionReferenceGenerator.generate())
                .account(account)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .description(request.getDescription() != null ? request.getDescription() : "Deposit")
                .status(TransactionStatus.SUCCESS)
                .balanceAfter(account.getBalance())
                .build();
        transactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(Long requestingUserId, WithdrawRequest request) {
        Account account = accountService.getOwnedAccountOrThrow(request.getAccountId(), requestingUserId);
        assertActive(account);

        BigDecimal amount = validateAmount(request.getAmount());

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this withdrawal.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionReference(TransactionReferenceGenerator.generate())
                .account(account)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL)
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal")
                .status(TransactionStatus.SUCCESS)
                .balanceAfter(account.getBalance())
                .build();
        transactionRepository.save(transaction);

        return toResponse(transaction);
    }

    /**
     * Transfers money from the caller's own account to another account
     * identified by account number.
     *
     * Steps (mirrors the numbered requirements this project is built against):
     *  1. Caller is already authenticated by the time this method runs
     *     (enforced by Spring Security at the controller boundary).
     *  2. getOwnedAccountOrThrow verifies the sender account belongs to
     *     the logged-in user.
     *  3. The receiver account is looked up by account number.
     *  4/5. Both accounts must be ACTIVE.
     *  6. Amount is validated (positive, non-null).
     *  7. Sender must have sufficient balance.
     *  8/9. Debit sender, credit receiver.
     *  10. Two linked Transaction rows are written (one per account).
     *  11. A single successful response is returned - or, on any failure,
     *      @Transactional rolls back every write made so far.
     */
    @Transactional
    public TransactionResponse transferMoney(Long requestingUserId, TransferRequest request) {
        if (request.getFromAccountId() == null) {
            throw new InvalidTransactionException("Sender account id is required.");
        }

        Account sender = accountService.getOwnedAccountOrThrow(request.getFromAccountId(), requestingUserId);
        assertActive(sender);

        Account receiver = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Receiver account not found with number: " + request.getToAccountNumber()));

        if (receiver.getId().equals(sender.getId())) {
            throw new InvalidTransactionException("Cannot transfer money to the same account.");
        }
        assertActive(receiver);

        BigDecimal amount = validateAmount(request.getAmount());

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transfer.");
        }

        // Debit sender
        sender.setBalance(sender.getBalance().subtract(amount));
        accountRepository.save(sender);

        // Credit receiver
        receiver.setBalance(receiver.getBalance().add(amount));
        accountRepository.save(receiver);

        String sharedReference = TransactionReferenceGenerator.generate();
        String description = request.getDescription() != null ? request.getDescription() : "Fund transfer";

        Transaction debitLeg = Transaction.builder()
                .transactionReference(sharedReference)
                .account(sender)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .description(description + " to " + receiver.getAccountNumber())
                .status(TransactionStatus.SUCCESS)
                .balanceAfter(sender.getBalance())
                .build();
        transactionRepository.save(debitLeg);

        Transaction creditLeg = Transaction.builder()
                .transactionReference(sharedReference)
                .account(receiver)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .description(description + " from " + sender.getAccountNumber())
                .status(TransactionStatus.SUCCESS)
                .balanceAfter(receiver.getBalance())
                .build();
        transactionRepository.save(creditLeg);

        return toResponse(debitLeg);
    }
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForAccount(Long accountId, Long requestingUserId,
                                                                 String type, LocalDateTime from, LocalDateTime to) {
        // Ownership check re-used here too: a customer can only view their own history.
        accountService.getOwnedAccountOrThrow(accountId, requestingUserId);

        List<Transaction> transactions;
        if (from != null && to != null && type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {

            TransactionType parsedType = parseType(type);

            transactions = transactionRepository
                    .findByAccountIdAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                            accountId,
                            parsedType,
                            from,
                            to
                    );

        } else if (from != null && to != null) {

            transactions = transactionRepository
                    .findByAccountIdAndDateRange(accountId, from, to);

        } else if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {

            TransactionType parsedType = parseType(type);

            transactions = transactionRepository
                    .findByAccountIdAndTransactionTypeOrderByCreatedAtDesc(
                            accountId,
                            parsedType
                    );

        } else {

            transactions = transactionRepository
                    .findByAccountIdOrderByCreatedAtDesc(accountId);
        }

        return transactions.stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionDetails(Long transactionId, Long requestingUserId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new InvalidTransactionException("Transaction not found with id: " + transactionId));
        // Ownership check: the transaction's account must belong to the requester.
        accountService.getOwnedAccountOrThrow(transaction.getAccount().getId(), requestingUserId);
        return toResponse(transaction);
    }

    private void assertActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException(
                    "Account " + account.getAccountNumber() + " is " + account.getStatus() + " and cannot be used for transactions.");
        }
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero.");
        }
        return amount;
    }

    private TransactionType parseType(String raw) {
        try {
            return TransactionType.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            throw new InvalidTransactionException("Invalid transaction type filter: " + raw);
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
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

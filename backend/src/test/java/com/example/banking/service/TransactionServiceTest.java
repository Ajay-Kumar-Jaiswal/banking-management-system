package com.example.banking.service;

import com.example.banking.dto.DepositRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.WithdrawRequest;
import com.example.banking.entity.Account;
import com.example.banking.entity.User;
import com.example.banking.enums.AccountStatus;
import com.example.banking.enums.AccountType;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.exception.InvalidAccountStatusException;
import com.example.banking.exception.UnauthorizedAccountAccessException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * These tests focus heavily on the fund transfer service, per the project
 * requirements, plus the deposit/withdrawal edge cases (insufficient
 * balance, inactive account, unauthorized access to another customer's
 * account).
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    private User owner;
    private User otherUser;
    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).fullName("Asha Rao").build();
        otherUser = User.builder().id(2L).fullName("Ravi Kumar").build();

        senderAccount = Account.builder()
                .id(10L).accountNumber("AC1000000001").user(owner)
                .accountType(AccountType.SAVINGS).balance(new BigDecimal("50000.00"))
                .status(AccountStatus.ACTIVE).build();

        receiverAccount = Account.builder()
                .id(20L).accountNumber("AC2000000002").user(otherUser)
                .accountType(AccountType.SAVINGS).balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE).build();
    }

    // ---------- Deposit ----------

    @Test
    void deposit_increasesBalance_andCreatesSuccessTransaction() {
        DepositRequest request = new DepositRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("10000.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.deposit(1L, request);

        assertThat(senderAccount.getBalance()).isEqualByComparingTo("60000.00");
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("60000.00");
        assertThat(response.getTransactionType()).isEqualTo("DEPOSIT");
    }

    @Test
    void deposit_throwsInvalidAccountStatus_whenAccountIsSuspended() {
        senderAccount.setStatus(AccountStatus.SUSPENDED);
        DepositRequest request = new DepositRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("100.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);

        assertThatThrownBy(() -> transactionService.deposit(1L, request))
                .isInstanceOf(InvalidAccountStatusException.class);
    }

    // ---------- Withdrawal ----------

    @Test
    void withdraw_throwsInsufficientBalanceException_whenAmountExceedsBalance() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("999999.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);

        assertThatThrownBy(() -> transactionService.withdraw(1L, request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdraw_decreasesBalance_whenSufficientFunds() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("5000.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        transactionService.withdraw(1L, request);

        assertThat(senderAccount.getBalance()).isEqualByComparingTo("45000.00");
    }

    // ---------- Fund transfer (the most important feature) ----------

    @Test
    void transfer_movesMoneyBetweenAccounts_andWritesTwoLinkedTransactions() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(10L);
        request.setToAccountNumber("AC2000000002");
        request.setAmount(new BigDecimal("5000.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);
        when(accountRepository.findByAccountNumber("AC2000000002")).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.transferMoney(1L, request);

        assertThat(senderAccount.getBalance()).isEqualByComparingTo("45000.00");
        assertThat(receiverAccount.getBalance()).isEqualByComparingTo("6000.00");
        assertThat(response.getTransactionType()).isEqualTo("TRANSFER");

        // Two transaction legs written: one debit, one credit, sharing a reference.
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void transfer_throwsInsufficientBalance_andNeverTouchesReceiver_whenSenderLacksFunds() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(10L);
        request.setToAccountNumber("AC2000000002");
        request.setAmount(new BigDecimal("999999.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);
        when(accountRepository.findByAccountNumber("AC2000000002")).thenReturn(Optional.of(receiverAccount));

        assertThatThrownBy(() -> transactionService.transferMoney(1L, request))
                .isInstanceOf(InsufficientBalanceException.class);

        // Balances must remain untouched - this is what @Transactional rollback guarantees
        // in the real database; here we assert the in-memory state was never mutated
        // because the check happens before any debit/credit occurs.
        assertThat(senderAccount.getBalance()).isEqualByComparingTo("50000.00");
        assertThat(receiverAccount.getBalance()).isEqualByComparingTo("1000.00");
        verify(accountRepository, never()).save(any());
    }

    @Test
    void transfer_throwsAccountNotFound_whenReceiverAccountNumberDoesNotExist() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(10L);
        request.setToAccountNumber("AC9999999999");
        request.setAmount(new BigDecimal("100.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);
        when(accountRepository.findByAccountNumber("AC9999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transferMoney(1L, request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transfer_throwsInvalidAccountStatus_whenReceiverAccountIsSuspended() {
        receiverAccount.setStatus(AccountStatus.SUSPENDED);
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(10L);
        request.setToAccountNumber("AC2000000002");
        request.setAmount(new BigDecimal("100.00"));

        when(accountService.getOwnedAccountOrThrow(10L, 1L)).thenReturn(senderAccount);
        when(accountRepository.findByAccountNumber("AC2000000002")).thenReturn(Optional.of(receiverAccount));

        assertThatThrownBy(() -> transactionService.transferMoney(1L, request))
                .isInstanceOf(InvalidAccountStatusException.class);
    }

    @Test
    void transfer_delegatesOwnershipCheckToAccountService_soUnauthorizedAccessIsRejected() {
        // Simulates user 1 trying to transfer FROM an account owned by user 2.
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(999L);
        request.setToAccountNumber("AC2000000002");
        request.setAmount(new BigDecimal("100.00"));

        when(accountService.getOwnedAccountOrThrow(999L, 1L))
                .thenThrow(new UnauthorizedAccountAccessException("You do not have permission to access this account."));

        assertThatThrownBy(() -> transactionService.transferMoney(1L, request))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }
}

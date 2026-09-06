package com.example.banking.service;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.Account;
import com.example.banking.entity.User;
import com.example.banking.enums.AccountStatus;
import com.example.banking.enums.AccountType;
import com.example.banking.exception.UnauthorizedAccountAccessException;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User owner;
    private Account account;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).fullName("Asha Rao").build();
        account = Account.builder()
                .id(10L).accountNumber("AC1000000001").user(owner)
                .accountType(AccountType.SAVINGS).balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE).build();
    }

    @Test
    void createAccount_generatesUniqueAccountNumber_andPersistsWithZeroBalance() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountType("SAVINGS");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        AccountResponse response = accountService.createAccount(1L, request);

        assertThat(response.getAccountId()).isEqualTo(99L);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getAccountNumber()).startsWith("AC");
    }

    @Test
    void createAccount_throwsUserNotFoundException_whenUserDoesNotExist() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountType("SAVINGS");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.createAccount(1L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getOwnedAccountOrThrow_returnsAccount_whenRequesterIsOwner() {
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        Account result = accountService.getOwnedAccountOrThrow(10L, 1L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getOwnedAccountOrThrow_throwsUnauthorized_whenRequesterIsNotOwner() {
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.getOwnedAccountOrThrow(10L, 2L))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }
}

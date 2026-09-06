package com.example.banking.service;

import com.example.banking.dto.BeneficiaryRequest;
import com.example.banking.dto.BeneficiaryResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.Beneficiary;
import com.example.banking.entity.User;
import com.example.banking.enums.AccountStatus;
import com.example.banking.enums.AccountType;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InvalidAccountStatusException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.BeneficiaryRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;

    @InjectMocks
    private BeneficiaryService beneficiaryService;

    private User user;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).fullName("Asha Rao").build();
        destinationAccount = Account.builder()
                .id(20L).accountNumber("AC2000000002")
                .accountType(AccountType.SAVINGS).balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE).build();
    }

    @Test
    void addBeneficiary_savesBeneficiary_whenDestinationAccountIsActive() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setName("Ravi Kumar");
        request.setAccountNumber("AC2000000002");
        request.setBankName("Example Bank");
        request.setIfscCode("EXBK0001234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByAccountNumber("AC2000000002")).thenReturn(Optional.of(destinationAccount));
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(inv -> {
            Beneficiary b = inv.getArgument(0);
            b.setId(5L);
            return b;
        });

        BeneficiaryResponse response = beneficiaryService.addBeneficiary(1L, request);

        assertThat(response.getBeneficiaryId()).isEqualTo(5L);
        assertThat(response.getAccountNumber()).isEqualTo("AC2000000002");
    }

    @Test
    void addBeneficiary_throwsAccountNotFound_whenDestinationDoesNotExist() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setName("Ravi Kumar");
        request.setAccountNumber("AC9999999999");
        request.setBankName("Example Bank");
        request.setIfscCode("EXBK0001234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByAccountNumber("AC9999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.addBeneficiary(1L, request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void addBeneficiary_throwsInvalidAccountStatus_whenDestinationIsSuspended() {
        destinationAccount.setStatus(AccountStatus.SUSPENDED);
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setName("Ravi Kumar");
        request.setAccountNumber("AC2000000002");
        request.setBankName("Example Bank");
        request.setIfscCode("EXBK0001234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByAccountNumber("AC2000000002")).thenReturn(Optional.of(destinationAccount));

        assertThatThrownBy(() -> beneficiaryService.addBeneficiary(1L, request))
                .isInstanceOf(InvalidAccountStatusException.class);
    }
}

package com.example.banking.service;

import com.example.banking.dto.BeneficiaryRequest;
import com.example.banking.dto.BeneficiaryResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.Beneficiary;
import com.example.banking.entity.User;
import com.example.banking.enums.AccountStatus;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.BeneficiaryNotFoundException;
import com.example.banking.exception.InvalidAccountStatusException;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.BeneficiaryRepository;
import com.example.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public BeneficiaryResponse addBeneficiary(Long userId, BeneficiaryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Verify the destination account actually exists and is active before
        // letting the customer save it as a beneficiary - catches typos early.
        Account destination = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with number: " + request.getAccountNumber()));

        if (destination.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException("Destination account is not active and cannot be added as a beneficiary.");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .user(user)
                .name(request.getName())
                .accountNumber(request.getAccountNumber())
                .bankName(request.getBankName())
                .ifscCode(request.getIfscCode())
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return toResponse(saved);
    }

    public List<BeneficiaryResponse> getBeneficiariesForUser(Long userId) {
        return beneficiaryRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteBeneficiary(Long beneficiaryId, Long userId) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(beneficiaryId, userId)
                .orElseThrow(() -> new BeneficiaryNotFoundException(
                        "Beneficiary not found or does not belong to the current user."));
        beneficiaryRepository.delete(beneficiary);
    }

    private BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .beneficiaryId(beneficiary.getId())
                .name(beneficiary.getName())
                .accountNumber(beneficiary.getAccountNumber())
                .bankName(beneficiary.getBankName())
                .ifscCode(beneficiary.getIfscCode())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}

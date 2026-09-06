package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BeneficiaryResponse {
    private Long beneficiaryId;
    private String name;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private LocalDateTime createdAt;
}

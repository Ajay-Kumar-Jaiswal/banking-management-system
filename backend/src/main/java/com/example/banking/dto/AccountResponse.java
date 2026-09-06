package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AccountResponse {
    private Long accountId;
    private String accountNumber;
    private Long customerId;
    private String customerName;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
}

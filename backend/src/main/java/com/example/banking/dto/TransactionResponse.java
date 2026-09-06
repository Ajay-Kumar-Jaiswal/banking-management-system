package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TransactionResponse {
    private Long transactionId;
    private String transactionReference;
    private Long accountId;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
    private String description;
    private String status;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}

package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    @NotNull(message = "Sender account id is required")
    private Long fromAccountId;

    /**
     * Destination is identified by account number (not internal id) because
     * that mirrors how a real transfer form works - the customer types in
     * the account number of the person they are paying.
     */
    @NotBlank(message = "Receiver account number is required")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String description;
}

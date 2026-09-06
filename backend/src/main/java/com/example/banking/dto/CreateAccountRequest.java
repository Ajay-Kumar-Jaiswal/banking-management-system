package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private String accountType; // SAVINGS or CURRENT, validated in service layer
}

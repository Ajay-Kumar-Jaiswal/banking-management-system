package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeneficiaryRequest {

    @NotBlank(message = "Beneficiary name is required")
    private String name;

    @NotBlank(message = "Beneficiary account number is required")
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "IFSC code is required")
    private String ifscCode;
}

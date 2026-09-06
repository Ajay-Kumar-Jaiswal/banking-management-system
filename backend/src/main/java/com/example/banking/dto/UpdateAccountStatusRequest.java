package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // ACTIVE, SUSPENDED, CLOSED
}

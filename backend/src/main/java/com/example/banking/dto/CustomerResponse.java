package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
    private LocalDateTime createdAt;
}

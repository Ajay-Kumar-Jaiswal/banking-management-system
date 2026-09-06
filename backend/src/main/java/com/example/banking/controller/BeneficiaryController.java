package com.example.banking.controller;

import com.example.banking.dto.BeneficiaryRequest;
import com.example.banking.dto.BeneficiaryResponse;
import com.example.banking.security.CustomUserDetails;
import com.example.banking.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @Valid @RequestBody BeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getMyBeneficiaries(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(beneficiaryService.getBeneficiariesForUser(principal.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(@AuthenticationPrincipal CustomUserDetails principal,
                                                   @PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

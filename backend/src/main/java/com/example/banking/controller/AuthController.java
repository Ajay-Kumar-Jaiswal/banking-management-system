package com.example.banking.controller;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.LoginResponse;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.entity.User;
import com.example.banking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public endpoints - no authentication required.
 * Controllers stay thin: validate the shape of the request (via @Valid)
 * and delegate everything else to the service layer.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        User created = authService.register(request);
        Map<String, Object> body = Map.of(
                "userId", created.getId(),
                "email", created.getEmail(),
                "message", "Registration successful. Please log in."
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

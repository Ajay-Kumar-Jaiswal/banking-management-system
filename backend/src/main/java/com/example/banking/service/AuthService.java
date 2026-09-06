package com.example.banking.service;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.LoginResponse;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.entity.User;
import com.example.banking.enums.Role;
import com.example.banking.exception.DuplicateEmailException;
import com.example.banking.exception.InvalidCredentialsException;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.CustomUserDetails;
import com.example.banking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration and login.
 *
 * Passwords are never stored or compared as plain text: BCryptPasswordEncoder
 * hashes the password with a random salt at registration time, and
 * AuthenticationManager (backed by DaoAuthenticationProvider) verifies a
 * login attempt by re-hashing the supplied password and comparing hashes.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("An account with email '" + request.getEmail() + "' already exists.");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .role(Role.CUSTOMER)
                .build();

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

package com.example.banking.service;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.LoginResponse;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.entity.User;
import com.example.banking.enums.Role;
import com.example.banking.exception.DuplicateEmailException;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Asha Rao");
        registerRequest.setEmail("asha@example.com");
        registerRequest.setPhoneNumber("9876543210");
        registerRequest.setPassword("StrongPass123");
        registerRequest.setAddress("12 MG Road, Bengaluru");
    }

    @Test
    void register_savesUserWithHashedPassword_whenEmailIsNew() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = authService.register(registerRequest);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);
        verify(passwordEncoder).encode("StrongPass123");
    }

    @Test
    void register_throwsDuplicateEmailException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_throwsInvalidCredentialsException_whenAuthenticationFails() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("asha@example.com");
        loginRequest.setPassword("wrong-password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(com.example.banking.exception.InvalidCredentialsException.class);
    }

    @Test
    void login_returnsTokenAndUserInfo_whenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("asha@example.com");
        loginRequest.setPassword("StrongPass123");

        User user = User.builder()
                .id(1L).fullName("Asha Rao").email("asha@example.com")
                .passwordHash("hashed").role(Role.CUSTOMER).build();

        when(userRepository.findByEmail("asha@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any())).thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getEmail()).isEqualTo("asha@example.com");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }
}

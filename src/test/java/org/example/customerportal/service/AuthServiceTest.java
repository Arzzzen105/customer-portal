package org.example.customerportal.service;

import org.example.customerportal.model.dto.LoginResponse;
import org.example.customerportal.model.entity.Customer;
import org.example.customerportal.model.request.LoginRequest;
import org.example.customerportal.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("AC-001: Should authenticate customer successfully and set SecurityContext")
    void shouldAuthenticateCustomerSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .email("  Customer@Example.COM  ")
                .password("Password123!")
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .email("customer@example.com")
                .passwordHash("encodedPasswordHash")
                .role("CUSTOMER")
                .enabled(true)
                .build();

        when(customerRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("Password123!", "encodedPasswordHash")).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("customer@example.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("customer@example.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("AC-002: Should throw BadCredentialsException when password does not match")
    void shouldThrowBadCredentialsOnPasswordMismatch() {
        LoginRequest request = LoginRequest.builder()
                .email("customer@example.com")
                .password("WrongPassword!")
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .email("customer@example.com")
                .passwordHash("encodedPasswordHash")
                .role("CUSTOMER")
                .enabled(true)
                .build();

        when(customerRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("WrongPassword!", "encodedPasswordHash")).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("AC-003: Should throw BadCredentialsException when account does not exist")
    void shouldThrowBadCredentialsOnMissingAccount() {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("Password123!")
                .build();

        when(customerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("AC-004: Should throw BadCredentialsException when customer account is disabled")
    void shouldThrowBadCredentialsOnDisabledAccount() {
        LoginRequest request = LoginRequest.builder()
                .email("disabled@example.com")
                .password("Password123!")
                .build();

        Customer customer = Customer.builder()
                .id(2L)
                .email("disabled@example.com")
                .passwordHash("encodedPasswordHash")
                .role("CUSTOMER")
                .enabled(false)
                .build();

        when(customerRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("Password123!", "encodedPasswordHash")).thenReturn(true);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }
}

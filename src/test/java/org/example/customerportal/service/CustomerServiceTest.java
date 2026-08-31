package org.example.customerportal.service;

import org.example.customerportal.exception.DuplicateEmailException;
import org.example.customerportal.model.dto.CustomerResponse;
import org.example.customerportal.model.entity.Customer;
import org.example.customerportal.model.request.RegisterCustomerRequest;
import org.example.customerportal.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("Should normalize email, hash password, and save customer successfully")
    void shouldRegisterCustomerSuccessfully() {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .email("  Jane.Doe@Example.COM  ")
                .password("ComplexPass123!")
                .build();

        when(customerRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("ComplexPass123!")).thenReturn("encodedHashedPassword60Chars");

        Customer savedCustomer = Customer.builder()
                .id(1L)
                .email("jane.doe@example.com")
                .passwordHash("encodedHashedPassword60Chars")
                .role("CUSTOMER")
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.registerCustomer(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        assertNotNull(response.getCreatedAt());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer captured = customerCaptor.getValue();
        assertEquals("jane.doe@example.com", captured.getEmail());
        assertEquals("encodedHashedPassword60Chars", captured.getPasswordHash());
        assertEquals("CUSTOMER", captured.getRole());
        assertTrue(captured.isEnabled());
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void shouldThrowDuplicateEmailExceptionWhenEmailExists() {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .email("duplicate@example.com")
                .password("ComplexPass123!")
                .build();

        when(customerRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> customerService.registerCustomer(request)
        );

        assertEquals("An account with this email already exists.", exception.getMessage());
        verify(customerRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}

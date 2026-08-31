package org.example.customerportal.service;

import lombok.RequiredArgsConstructor;
import org.example.customerportal.exception.DuplicateEmailException;
import org.example.customerportal.model.dto.CustomerResponse;
import org.example.customerportal.model.entity.Customer;
import org.example.customerportal.model.request.RegisterCustomerRequest;
import org.example.customerportal.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerResponse registerCustomer(RegisterCustomerRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Customer customer = Customer.builder()
                .email(normalizedEmail)
                .passwordHash(encodedPassword)
                .role("CUSTOMER")
                .enabled(true)
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerResponse.builder()
                .id(savedCustomer.getId())
                .email(savedCustomer.getEmail())
                .role(savedCustomer.getRole())
                .createdAt(savedCustomer.getCreatedAt())
                .build();
    }
}

package org.example.customerportal.service;

import lombok.RequiredArgsConstructor;
import org.example.customerportal.model.dto.LoginResponse;
import org.example.customerportal.model.entity.Customer;
import org.example.customerportal.model.request.LoginRequest;
import org.example.customerportal.repository.CustomerRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Customer customer = customerRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!customer.isEnabled()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String roleName = customer.getRole().startsWith("ROLE_")
                ? customer.getRole()
                : "ROLE_" + customer.getRole();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                customer.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(roleName))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return LoginResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .role(customer.getRole())
                .build();
    }
}

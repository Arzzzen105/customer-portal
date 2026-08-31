package org.example.customerportal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.customerportal.model.dto.CustomerResponse;
import org.example.customerportal.model.request.RegisterCustomerRequest;
import org.example.customerportal.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {

        CustomerResponse response = customerService.registerCustomer(request);
        URI location = URI.create("/api/v1/customers/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }
}

package org.example.customerportal.controller;

import org.example.customerportal.exception.GlobalExceptionHandler;
import org.example.customerportal.model.dto.CustomerResponse;
import org.example.customerportal.model.request.RegisterCustomerRequest;
import org.example.customerportal.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Unit / Slice Tests")
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK with CustomerResponse when getCustomerProfile succeeds")
    void shouldReturnCustomerProfileWhenFound() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .id(1L)
                .email("alice@example.com")
                .role("CUSTOMER")
                .createdAt(Instant.parse("2026-09-05T10:00:00Z"))
                .build();

        when(customerService.getCustomerProfile(eq(1L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.createdAt", is("2026-09-05T10:00:00Z")));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when path variable is non-numeric")
    void shouldReturn400WhenPathVariableIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/customers/abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Failed to convert value of type")));
    }

    @Test
    @DisplayName("Should return 201 Created with Location header when registering customer")
    void shouldReturn201OnValidRegistration() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .id(1L)
                .email("jane.doe@example.com")
                .role("CUSTOMER")
                .createdAt(Instant.parse("2026-09-05T10:00:00Z"))
                .build();

        when(customerService.registerCustomer(any(RegisterCustomerRequest.class))).thenReturn(response);

        String requestJson = """
                {
                    "email": "jane.doe@example.com",
                    "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/customers/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("jane.doe@example.com")));
    }
}

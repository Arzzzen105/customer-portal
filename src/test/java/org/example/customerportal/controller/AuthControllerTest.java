package org.example.customerportal.controller;

import org.example.customerportal.exception.GlobalExceptionHandler;
import org.example.customerportal.model.dto.LoginResponse;
import org.example.customerportal.model.request.LoginRequest;
import org.example.customerportal.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit / Slice Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK with LoginResponse on valid request")
    void shouldReturn200OnValidLogin() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .id(1L)
                .email("customer@example.com")
                .role("CUSTOMER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        String requestJson = """
                {
                    "email": "customer@example.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("customer@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is invalid")
    void shouldReturn400OnInvalidEmail() throws Exception {
        String requestJson = """
                {
                    "email": "invalid-email",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors", not(empty())));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password is blank")
    void shouldReturn400OnBlankPassword() throws Exception {
        String requestJson = """
                {
                    "email": "customer@example.com",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors", not(empty())));
    }
}

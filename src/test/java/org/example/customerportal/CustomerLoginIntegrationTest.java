package org.example.customerportal;

import org.example.customerportal.model.entity.Customer;
import org.example.customerportal.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Customer Login Integration Tests (US-002)")
public class CustomerLoginIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private Customer activeCustomer;
    private Customer disabledCustomer;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Seed an active customer
        activeCustomer = customerRepository.save(
            Customer.builder()
                .email("customer@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role("CUSTOMER")
                .enabled(true)
                .build()
        );

        // Seed a disabled customer
        disabledCustomer = customerRepository.save(
            Customer.builder()
                .email("disabled@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role("CUSTOMER")
                .enabled(false)
                .build()
        );
    }

    @Nested
    @DisplayName("AC-001: Successful Login")
    class SuccessfulLoginTests {

        @Test
        @DisplayName("Should return 200 OK with customer identity when valid credentials are submitted")
        void shouldAuthenticateCustomerSuccessfully() throws Exception {
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
                .andExpect(jsonPath("$.id", is(activeCustomer.getId().intValue())))
                .andExpect(jsonPath("$.email", is("customer@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
        }

        @Test
        @DisplayName("Should normalize email (trim whitespace and ignore case) on login")
        void shouldNormalizeEmailOnLogin() throws Exception {
            String requestJson = """
                {
                    "email": "  CUSTOMER@EXAMPLE.COM  ",
                    "password": "Password123!"
                }
                """;

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(activeCustomer.getId().intValue())))
                .andExpect(jsonPath("$.email", is("customer@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
        }
    }

    @Nested
    @DisplayName("AC-002: Invalid Password")
    class InvalidPasswordTests {

        @Test
        @DisplayName("Should return 401 Unauthorized with uniform error message when password is wrong")
        void shouldRejectInvalidPassword() throws Exception {
            String requestJson = """
                {
                    "email": "customer@example.com",
                    "password": "WrongPassword999!"
                }
                """;

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid email or password")))
                .andExpect(jsonPath("$.path", is("/api/v1/auth/login")));
        }
    }

    @Nested
    @DisplayName("AC-003: Unknown Account")
    class UnknownAccountTests {

        @Test
        @DisplayName("Should return 401 Unauthorized with uniform error message when account does not exist")
        void shouldRejectNonExistentAccount() throws Exception {
            String requestJson = """
                {
                    "email": "nonexistent@example.com",
                    "password": "AnyPassword123!"
                }
                """;

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid email or password")))
                .andExpect(jsonPath("$.path", is("/api/v1/auth/login")));
        }
    }

    @Nested
    @DisplayName("AC-004: Disabled Account")
    class DisabledAccountTests {

        @Test
        @DisplayName("Should return 401 Unauthorized with uniform error message when account is disabled")
        void shouldRejectDisabledAccountLogin() throws Exception {
            String requestJson = """
                {
                    "email": "disabled@example.com",
                    "password": "Password123!"
                }
                """;

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid email or password")))
                .andExpect(jsonPath("$.path", is("/api/v1/auth/login")));
        }
    }

    @Nested
    @DisplayName("AC-005 & Security: Credential Sanitization and Media Type")
    class SecurityAndSanitizationTests {

        @Test
        @DisplayName("Should never expose password or hash in response payload or headers")
        void shouldNeverExposePasswordData() throws Exception {
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
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
        }

        @Test
        @DisplayName("Should reject non-JSON Content-Type with 415 Unsupported Media Type")
        void shouldRejectUnsupportedMediaType() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("email=customer@example.com&password=Password123!"))
                .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("Input Validation Rules")
    class InputValidationTests {

        @Test
        @DisplayName("Should return 400 Bad Request when email is blank")
        void shouldRejectBlankEmail() throws Exception {
            String requestJson = """
                {
                    "email": "",
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
        @DisplayName("Should return 400 Bad Request when email format is invalid")
        void shouldRejectInvalidEmailFormat() throws Exception {
            String requestJson = """
                {
                    "email": "invalid-email-format",
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
        void shouldRejectBlankPassword() throws Exception {
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
}

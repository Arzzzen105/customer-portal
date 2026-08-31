package org.example.customerportal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Customer Registration Integration Tests (US-001)")
public class CustomerRegistrationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private org.example.customerportal.repository.CustomerRepository customerRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("AC-001: Successful Registration")
    class SuccessfulRegistrationTests {

        @Test
        @DisplayName("Should return 201 Created with Location header and customer payload when valid data is submitted")
        void shouldRegisterNewCustomerSuccessfully() throws Exception {
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
                .andExpect(header().string("Location", matchesPattern("/api/v1/customers/\\d+")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("jane.doe@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }
    }

    @Nested
    @DisplayName("AC-002: Unique Email Duplicate Prevention")
    class UniqueEmailTests {

        @Test
        @DisplayName("Should return 409 Conflict when attempting to register with an already registered email")
        void shouldRejectDuplicateEmailRegistration() throws Exception {
            String firstRequest = """
                {
                    "email": "duplicate.test@example.com",
                    "password": "Password12345!"
                }
                """;

            // First registration
            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(firstRequest))
                .andExpect(status().isCreated());

            // Second registration with same email (case-insensitive test)
            String duplicateRequest = """
                {
                    "email": "DUPLICATE.TEST@EXAMPLE.COM",
                    "password": "AnotherPassword123!"
                }
                """;

            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(duplicateRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("already exists")))
                .andExpect(jsonPath("$.path", is("/api/v1/customers")));
        }
    }

    @Nested
    @DisplayName("AC-003: Email and Password Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should return 400 Bad Request when email format is invalid")
        void shouldRejectInvalidEmailFormat() throws Exception {
            String requestJson = """
                {
                    "email": "invalid-email-format",
                    "password": "StrongPassword123!"
                }
                """;

            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors", not(empty())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email is blank")
        void shouldRejectBlankEmail() throws Exception {
            String requestJson = """
                {
                    "email": "",
                    "password": "StrongPassword123!"
                }
                """;

            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors", not(empty())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when password is shorter than 12 characters")
        void shouldRejectShortPassword() throws Exception {
            String requestJson = """
                {
                    "email": "user@example.com",
                    "password": "Short1!"
                }
                """;

            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors", not(empty())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when password lacks required character classes")
        void shouldRejectWeakPassword() throws Exception {
            String requestJson = """
                {
                    "email": "user@example.com",
                    "password": "alllowercaseandnumbers12345"
                }
                """;

            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors", not(empty())));
        }
    }

    @Nested
    @DisplayName("AC-004 & AC-005: Security & Payload Sanitization")
    class SecuritySanitizationTests {

        @Test
        @DisplayName("Should never expose password or password hash in response payload or headers")
        void shouldNeverExposePasswordDataInResponse() throws Exception {
            String requestJson = """
                {
                    "email": "security.audit@example.com",
                    "password": "SuperSecretPassword123!"
                }
                """;

            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
        }

        @Test
        @DisplayName("Should reject request with 415 Unsupported Media Type if Content-Type is not application/json")
        void shouldRejectUnsupportedMediaType() throws Exception {
            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("email=user@example.com&password=Password123!"))
                .andExpect(status().isUnsupportedMediaType());
        }
    }
}

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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Customer Profile View Integration Tests (US-003)")
public class CustomerProfileIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private Customer customerOne;
    private Customer customerTwo;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Seed customer 1
        customerOne = customerRepository.save(
            Customer.builder()
                .email("alice@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role("CUSTOMER")
                .enabled(true)
                .build()
        );

        // Seed customer 2
        customerTwo = customerRepository.save(
            Customer.builder()
                .email("bob@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role("CUSTOMER")
                .enabled(true)
                .build()
        );
    }

    @Nested
    @DisplayName("AC-001: View Own Profile")
    class ViewOwnProfileTests {

        @Test
        @DisplayName("Should return 200 OK with profile data when authenticated customer requests their own ID")
        void shouldReturnProfileWhenCustomerViewsOwnData() throws Exception {
            mockMvc.perform(get("/api/v1/customers/{id}", customerOne.getId())
                    .with(user(customerOne.getEmail()).roles("CUSTOMER"))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(customerOne.getId().intValue())))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
        }
    }

    @Nested
    @DisplayName("AC-002: Ownership Enforcement")
    class OwnershipEnforcementTests {

        @Test
        @DisplayName("Should return 403 Forbidden with standard error body when requesting another customer's profile")
        void shouldReturn403ForbiddenWhenAccessingAnotherCustomerProfile() throws Exception {
            mockMvc.perform(get("/api/v1/customers/{id}", customerTwo.getId())
                    .with(user(customerOne.getEmail()).roles("CUSTOMER"))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Access denied")))
                .andExpect(jsonPath("$.path", is("/api/v1/customers/" + customerTwo.getId())));
        }
    }

    @Nested
    @DisplayName("AC-003: Sensitive Data Exclusion")
    class SensitiveDataExclusionTests {

        @Test
        @DisplayName("Should never include password, passwordHash, or password_hash in response body")
        void shouldNeverExposePasswordDataInProfileResponse() throws Exception {
            mockMvc.perform(get("/api/v1/customers/{id}", customerOne.getId())
                    .with(user(customerOne.getEmail()).roles("CUSTOMER"))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
        }
    }

    @Nested
    @DisplayName("AC-004: Consistent Response & Error Scenarios")
    class ConsistentResponseTests {

        @Test
        @DisplayName("Should return 401 Unauthorized with AC-6 error body when request is unauthenticated")
        void shouldReturn401UnauthorizedWhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/customers/{id}", customerOne.getId())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Full authentication is required to access this resource")))
                .andExpect(jsonPath("$.path", is("/api/v1/customers/" + customerOne.getId())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request with AC-6 error body when path variable id is non-numeric")
        void shouldReturn400BadRequestWhenIdIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/v1/customers/abc")
                    .with(user(customerOne.getEmail()).roles("CUSTOMER"))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path", is("/api/v1/customers/abc")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when customer record is missing from database for caller")
        void shouldReturn404NotFoundWhenOwnProfileDoesNotExist() throws Exception {
            // Delete customerOne from repository to simulate missing entity for authenticated user
            customerRepository.delete(customerOne);

            mockMvc.perform(get("/api/v1/customers/{id}", customerOne.getId())
                    .with(user(customerOne.getEmail()).roles("CUSTOMER"))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Customer not found")))
                .andExpect(jsonPath("$.path", is("/api/v1/customers/" + customerOne.getId())));
        }
    }
}

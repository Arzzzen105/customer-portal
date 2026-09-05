---
artifact_type: test_strategy
story: US-002
version: 1
status: DRAFT
created_at: 2026-09-02T13:38:00Z
updated_at: 2026-09-02T13:38:00Z
produced_by: test-writer
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/designs/api/US-002-api-design.md
    version: 1
  - path: docs/designs/api/US-002-openapi.yaml
    version: 1
  - path: docs/designs/database/US-002-db-design.md
    version: 1
  - path: docs/designs/database/US-002-entity-model.md
    version: 1
  - path: docs/impact-analysis/US-002-impact-analysis.md
    version: 1
  - path: docs/plans/US-002-implementation-plan.md
    version: 1
  - path: docs/reviews/plans/US-002-plan-review.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
---

# Test Strategy — US-002: Customer Login

## 1. Scope & Objective

This test strategy defines the automated testing approach for **US-002: Customer Login**. It ensures executable test coverage for all Acceptance Criteria (`AC-001` through `AC-005`), API contracts, input validation constraints, anti-enumeration security rules, session context establishment, and response sanitization before and during production implementation.

---

## 2. Selected Test Levels

1. **End-to-End Integration & Contract Tests (`@SpringBootTest` + `MockMvc`):**
   - Class: `org.example.customerportal.CustomerLoginIntegrationTest`
   - Purpose: Verifies the entire HTTP pipeline, Spring Security filter chain (`permitAll` on `/api/v1/auth/login`), JSON serialization, status codes, uniform `401 Unauthorized` anti-enumeration handling, and database interaction.
2. **Web Slice Tests (`@WebMvcTest`):**
   - Class: `org.example.customerportal.controller.AuthControllerTest` (planned during implementation)
   - Purpose: Verifies controller layer routing, `@Valid` Jakarta Bean Validation error mapping (`400 Bad Request`), and media type checking (`415 Unsupported Media Type`).
3. **Unit Tests (POJO / JUnit 5 + Mockito):**
   - Class: `org.example.customerportal.service.AuthServiceTest` (planned during implementation)
   - Purpose: Verifies isolated business logic, email normalization (trimming whitespace and lowercase conversion per `OD-004`), credential verification with `PasswordEncoder.matches()`, and account enabled checks (`BR-004`).

---

## 3. Test Scenarios by Acceptance Criteria

### AC-001: Successful Login
- **Positive Scenarios:**
  - Submit registered email and valid password (`customer@example.com`, `Password123!`) to `POST /api/v1/auth/login`.
  - Submit email with surrounding whitespace and mixed casing (`"  CUSTOMER@EXAMPLE.COM  "`, `Password123!`).
- **Expected Outcome:** `200 OK`, response body containing `{ "id": 1, "email": "customer@example.com", "role": "CUSTOMER" }`.
- **Security Context:** Customer is authenticated with granted authority `ROLE_CUSTOMER`.

### AC-002: Invalid Password
- **Negative Scenario:** Submit registered email with wrong password (`WrongPassword999!`).
- **Expected Outcome:** `401 Unauthorized`, response body following AC-6 structure (`status: 401`, `error: "Unauthorized"`, `message: "Invalid email or password"`, `path: "/api/v1/auth/login"`).

### AC-003: Unknown Account
- **Negative Scenario:** Submit unregistered email (`nonexistent@example.com`, `AnyPassword123!`).
- **Expected Outcome:** `401 Unauthorized`, response body identical to AC-002 (`message: "Invalid email or password"`), preventing account enumeration.

### AC-004: Disabled Account
- **Negative Scenario:** Submit valid credentials for a customer record where `enabled = false` (`disabled@example.com`, `Password123!`).
- **Expected Outcome:** `401 Unauthorized`, uniform error message `"Invalid email or password"` per `BR-004` and `OD-002`.

### AC-005: Secure Authentication Response
- **Security Scenarios:**
  - Assert `password`, `passwordHash`, and `password_hash` do not exist in JSON response payloads across both successful and failed authentication attempts.
  - Submit non-JSON request body (`text/plain`) -> returns `415 Unsupported Media Type`.

### Input Validation Scenarios:
- Blank email (`""`) -> `400 Bad Request` with `fieldErrors`.
- Malformed email (`"invalid-email-format"`) -> `400 Bad Request` with `fieldErrors`.
- Blank password (`""`) -> `400 Bad Request` with `fieldErrors`.

---

## 4. Test Fixtures & Isolation

- Integration tests use isolated in-memory H2 database (`jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`).
- `@BeforeEach` clears database state via `customerRepository.deleteAll()` and seeds explicit test fixtures (`activeCustomer` and `disabledCustomer`) with BCrypt-encoded passwords.
- No reliance on external services, file databases, or execution order.

---

## 5. Excluded Scenarios & Limitations

- Multi-Factor Authentication (MFA), OAuth2 / Social Login, and Password Recovery are out of scope per User Story specification.
- Remember-Me persistent token generation and concurrent session concurrency controls are not part of MVP scope.

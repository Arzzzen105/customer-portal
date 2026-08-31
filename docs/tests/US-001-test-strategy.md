---
artifact_type: test_strategy
story: US-001
version: 1
status: DRAFT
created_at: 2026-08-31T16:11:40Z
updated_at: 2026-08-31T16:11:40Z
produced_by: test-writer
inputs:
  - path: docs/stories/US-001-register-customer.md
    version: null
  - path: docs/specifications/US-001-spec.md
    version: 1
  - path: docs/designs/api/US-001-api-design.md
    version: 1
  - path: docs/designs/api/US-001-openapi.yaml
    version: 1
  - path: docs/designs/database/US-001-db-design.md
    version: 1
  - path: docs/impact-analysis/US-001-impact-analysis.md
    version: 1
  - path: docs/plans/US-001-implementation-plan.md
    version: 1
  - path: docs/reviews/plans/US-001-plan-review.md
    version: 1
supersedes: null
---

# Test Strategy — US-001: Customer Registration

## 1. Scope & Objective

This test strategy defines the automated verification approach for **US-001: Customer Registration**. It ensures executable evidence covers all Acceptance Criteria (`AC-001` through `AC-005`), API contracts, database constraints, input validations, security boundaries, and error models prior to and during production implementation.

---

## 2. Selected Test Levels

1. **End-to-End Integration & Contract Tests (`@SpringBootTest` + `MockMvc`):**
   - Class: `org.example.customerportal.CustomerRegistrationIntegrationTest`
   - Purpose: Verifies full Spring MVC pipeline, Security filter chain, Request/Response JSON serialization, HTTP status codes, headers, and database side effects.
2. **Web Slice Tests (`@WebMvcTest`):**
   - Class: `org.example.customerportal.controller.CustomerControllerTest` (unit/slice)
   - Purpose: Verifies Controller request binding, `@Valid` error mapping to 400 Bad Request, conflict mapping to 409, and media type checks.
3. **Persistence Slice Tests (`@DataJpaTest`):**
   - Class: `org.example.customerportal.repository.CustomerRepositoryTest`
   - Purpose: Verifies `Customer` entity mapping, database unique constraint `uq_customer_email`, and JPA auditing UTC timestamps.
4. **Unit Tests (POJO / Junit 5):**
   - Class: `org.example.customerportal.service.CustomerServiceTest`, `org.example.customerportal.validation.PasswordValidatorTest`
   - Purpose: Verifies email normalization (trim + lowercase), duplicate check logic, password hashing with BCrypt, and password complexity regex rules in isolation.

---

## 3. Test Scenarios by Acceptance Criteria

### AC-001: Successful Registration
- **Positive Scenario:** Submit valid email and valid complex password to `POST /api/v1/customers`.
- **Expected Outcome:** `201 Created`, `Location: /api/v1/customers/{id}`, response JSON containing `{id, email, role: "CUSTOMER", createdAt}`.
- **Persistence Verification:** Record inserted into `customer` table with `role = 'CUSTOMER'`, `enabled = TRUE`, `created_at` in UTC.

### AC-002: Unique Email Duplicate Prevention
- **Negative Scenario:** Register account `duplicate.test@example.com`, then attempt second registration with same email using different casing `DUPLICATE.TEST@EXAMPLE.COM`.
- **Expected Outcome:** `409 Conflict`, error response body adhering to AC-6 structure (`status: 409`, `error: "Conflict"`, `message: "An account with this email already exists."`, `path: "/api/v1/customers"`). No duplicate record in database.

### AC-003: Email and Password Validation
- **Validation Scenarios:**
  - Invalid email format (`"invalid-email"`) -> `400 Bad Request` with `fieldErrors`.
  - Blank email (`""`) -> `400 Bad Request`.
  - Short password (<12 chars, e.g. `"Short1!"`) -> `400 Bad Request`.
  - Weak password lacking character classes (no upper, digit, or special) -> `400 Bad Request`.

### AC-004 & AC-005: Security & Response Sanitization
- **Security Scenarios:**
  - Response body verification: Assert `password`, `passwordHash`, and `password_hash` do not exist in JSON response.
  - Database verification: Password is saved strictly as a BCrypt hash (length 60), never plaintext.
  - Media type check: Sending non-JSON payload returns `415 Unsupported Media Type`.

---

## 4. Test Fixtures & Isolation

- Tests run against an isolated in-memory H2 database (`jdbc:h2:mem:<dbname>;DB_CLOSE_DELAY=-1`).
- Clean state guaranteed per test method execution.
- No reliance on file database or cross-test state leakage.

---

## 5. Excluded Scenarios & Limitations

- Login authentication session establishment is Out of Scope for US-001 (deferred to login stories).
- Password reset and email activation links are Out of Scope.

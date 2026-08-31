---
artifact_type: pr_summary
story: US-001
version: 1
status: APPROVED
created_at: 2026-08-31T16:19:30Z
updated_at: 2026-08-31T16:27:10Z
produced_by: pr-preparer
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
  - path: docs/designs/database/US-001-entity-model.md
    version: 1
  - path: docs/impact-analysis/US-001-impact-analysis.md
    version: 1
  - path: docs/plans/US-001-implementation-plan.md
    version: 1
  - path: docs/evidence/US-001-implementation-report.md
    version: 1
  - path: docs/evidence/US-001-verification-report.md
    version: 1
  - path: docs/reviews/security/US-001-security-review.md
    version: 1
  - path: docs/reviews/reconciliation/US-001-reconciliation.md
    version: 1
  - path: docs/reconciliation/US-001-traceability.md
    version: 1
supersedes: null
---

# Pull Request Summary — US-001: Customer Registration

## 1. Story & Business Goal

- **Story ID:** `US-001`
- **Title:** Customer Registration
- **Business Goal:** Enable prospective customers to self-register securely by providing an email and password, creating an active customer account with default role `CUSTOMER`.
- **Target Branch:** `main`

---

## 2. Scope & Implemented Features

- **Public REST Endpoint:** `POST /api/v1/customers` accepting `RegisterCustomerRequest` and returning `201 Created` with `Location: /api/v1/customers/{id}` and `CustomerResponse`.
- **Validation:** Enforces valid email format, max 255 chars, and password complexity (12–72 chars, upper, lower, digit, special character).
- **Duplicate Prevention:** Case-insensitive email normalization and unique constraint validation returning `409 Conflict` (AC-6 format).
- **Security:** Passwords encoded strictly as BCrypt hashes before persistence; credentials completely excluded from response DTOs and logs.
- **Persistence:** H2 file-based persistence with hand-maintained `schema.sql` DDL (`pk_customer`, `uq_customer_email`) and JPA Auditing for UTC timestamps.

---

## 3. API Changes

- **Added:** `POST /api/v1/customers`
  - Request: `{"email": "string", "password": "string"}`
  - Response (201): `{"id": 1, "email": "string", "role": "CUSTOMER", "createdAt": "2026-08-31T..."}` with header `Location: /api/v1/customers/1`
  - Error (400): Standard validation error with `fieldErrors` array.
  - Error (409): `{"status": 409, "error": "Conflict", "message": "An account with this email already exists.", "path": "/api/v1/customers"}`
  - Error (415): `415 Unsupported Media Type` when Content-Type is not `application/json`.

---

## 4. Database Changes

- **Added Table:** `customer`
  - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
  - `email`: `VARCHAR(255) NOT NULL UNIQUE`
  - `password_hash`: `VARCHAR(60) NOT NULL`
  - `role`: `VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER'`
  - `enabled`: `BOOLEAN NOT NULL DEFAULT TRUE`
  - `created_at`: `TIMESTAMP WITH TIME ZONE NOT NULL`
  - `updated_at`: `TIMESTAMP WITH TIME ZONE NOT NULL`

---

## 5. Security Changes

- BCrypt password hashing (`BCryptPasswordEncoder`).
- Public permit for `POST /api/v1/customers` in `SecurityFilterChain`; all other endpoints secured with `.anyRequest().authenticated()`.
- H2 Web Console disabled in production profile.
- Sanitized error responses without stack traces.

---

## 6. Tests Executed & Evidence

- **Execution Command:** `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"; mvn test`
- **Result:** `BUILD SUCCESS` (18/18 tests passing, 0 failures, 0 errors).
- **Evidence References:**
  - `docs/evidence/US-001-test-generation-report.md`
  - `docs/evidence/US-001-verification-report.md`
  - `docs/reviews/security/US-001-security-review.md`

---

## 7. Acceptance Criteria Coverage

| Acceptance Criterion | Implementation | Test Method | Status |
|---|---|---|---|
| **AC-001 (Successful Registration)** | `CustomerController`, `CustomerService` | `CustomerRegistrationIntegrationTest.shouldRegisterNewCustomerSuccessfully` | **VERIFIED** |
| **AC-002 (Unique Email)** | `CustomerService`, `CustomerRepository` | `CustomerRegistrationIntegrationTest.shouldRejectDuplicateEmailRegistration` | **VERIFIED** |
| **AC-003 (Validation)** | `RegisterCustomerRequest`, `PasswordValidator` | `CustomerRegistrationIntegrationTest.ValidationTests`, `PasswordValidatorTest` | **VERIFIED** |
| **AC-004 (Password Storage)** | `CustomerService`, `BCryptPasswordEncoder` | `CustomerServiceTest.shouldRegisterCustomerSuccessfully` | **VERIFIED** |
| **AC-005 (Response Sanitization)** | `CustomerResponse` | `CustomerRegistrationIntegrationTest.shouldNeverExposePasswordDataInResponse` | **VERIFIED** |

---

## 8. PR Candidate Files

### Include (Production & Test Sources)
- `pom.xml`
- `src/main/resources/application.yaml`
- `src/main/resources/schema.sql`
- `src/main/java/org/example/customerportal/config/JpaAuditingConfig.java`
- `src/main/java/org/example/customerportal/controller/CustomerController.java`
- `src/main/java/org/example/customerportal/exception/DuplicateEmailException.java`
- `src/main/java/org/example/customerportal/exception/GlobalExceptionHandler.java`
- `src/main/java/org/example/customerportal/model/dto/CustomerResponse.java`
- `src/main/java/org/example/customerportal/model/dto/ErrorResponse.java`
- `src/main/java/org/example/customerportal/model/dto/FieldErrorDto.java`
- `src/main/java/org/example/customerportal/model/entity/Customer.java`
- `src/main/java/org/example/customerportal/model/request/RegisterCustomerRequest.java`
- `src/main/java/org/example/customerportal/repository/CustomerRepository.java`
- `src/main/java/org/example/customerportal/security/SecurityConfig.java`
- `src/main/java/org/example/customerportal/service/CustomerService.java`
- `src/main/java/org/example/customerportal/validation/PasswordValidator.java`
- `src/main/java/org/example/customerportal/validation/ValidPassword.java`
- `src/test/resources/application.yaml`
- `src/test/java/org/example/customerportal/CustomerRegistrationIntegrationTest.java`
- `src/test/java/org/example/customerportal/service/CustomerServiceTest.java`
- `src/test/java/org/example/customerportal/validation/PasswordValidatorTest.java`
- Workflow artifacts in `docs/`

### Exclude
- File database files under `data/` (runtime local artifacts).

---

## 9. Release Notes

- **User-Facing:** Added public customer self-registration endpoint at `POST /api/v1/customers`. Customers can create accounts with email and secure password.
- **Technical:** Initialized Spring Data JPA customer entity model with H2 persistence, hand-written DDL validation, BCrypt password encryption, and custom password validation.
- **Security:** Enforced strong password policy (12–72 characters, mixed case, digit, special symbol), sanitized API responses to omit password fields, and configured Spring Security.

---

## 10. Notes For Reviewers

- All Open Decisions (`OD-001` through `OD-004`) were resolved via Option A and verified.
- Integration tests run against an isolated in-memory H2 instance without modifying file-based storage.

---

## 11. Readiness Result

**`PASS`** — Fully ready for Pull Request creation and final human gate approval (`READY_FOR_PR`).

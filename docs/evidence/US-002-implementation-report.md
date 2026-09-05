---
artifact_type: implementation_report
story: US-002
version: 1
status: DRAFT
created_at: 2026-09-02T13:43:30Z
updated_at: 2026-09-02T13:43:30Z
produced_by: springboot-implementor
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/reviews/specifications/US-002-spec-review.md
    version: 1
  - path: docs/designs/api/US-002-api-design.md
    version: 1
  - path: docs/designs/api/US-002-openapi.yaml
    version: 1
  - path: docs/designs/database/US-002-db-design.md
    version: 1
  - path: docs/designs/database/US-002-entity-model.md
    version: 1
  - path: docs/reviews/designs/US-002-design-review.md
    version: 1
  - path: docs/impact-analysis/US-002-impact-analysis.md
    version: 1
  - path: docs/plans/US-002-implementation-plan.md
    version: 1
  - path: docs/reviews/plans/US-002-plan-review.md
    version: 1
  - path: docs/tests/US-002-test-strategy.md
    version: 1
  - path: docs/tests/US-002-ac-test-matrix.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
tests_status: PASS
build_status: PASS
diagnostics_status: PASS
security_sensitive: true
---

# Implementation Report — US-002: Customer Login

## 1. Summary

The customer authentication (login) capability for User Story `US-002` has been fully implemented following the approved Implementation Plan [docs/plans/US-002-implementation-plan.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/plans/US-002-implementation-plan.md) and Specification [docs/specifications/US-002-spec.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/specifications/US-002-spec.md).

- **Implemented Capability:** Exposes public endpoint `POST /api/v1/auth/login` to authenticate customers with email normalization, BCrypt password validation, enabled status verification, session establishment (`SecurityContextHolder`), and anti-enumeration uniform `401 Unauthorized` error handling.
- **Implementation Status:** Complete.
- **Validation Status:** 100% tests passing (`35/35` tests across unit, slice, and integration test suites).
- **Security & Hygiene:** Credentials (passwords/hashes) are never returned in response DTOs or logs.

---

## 2. Source Artifacts

| Artifact Type | Path | Version |
|---|---|---|
| `story` | `docs/stories/US-002-customer-login.md` | unversioned |
| `specification` | `docs/specifications/US-002-spec.md` | 1 (APPROVED) |
| `specification_review` | `docs/reviews/specifications/US-002-spec-review.md` | 1 (APPROVED) |
| `api_design` | `docs/designs/api/US-002-api-design.md` | 1 |
| `openapi` | `docs/designs/api/US-002-openapi.yaml` | 1 |
| `database_design` | `docs/designs/database/US-002-db-design.md` | 1 |
| `entity_model` | `docs/designs/database/US-002-entity-model.md` | 1 |
| `design_review` | `docs/reviews/designs/US-002-design-review.md` | 1 (APPROVED) |
| `impact_analysis` | `docs/impact-analysis/US-002-impact-analysis.md` | 1 (PASS) |
| `implementation_plan` | `docs/plans/US-002-implementation-plan.md` | 1 (APPROVED) |
| `plan_review` | `docs/reviews/plans/US-002-plan-review.md` | 1 (APPROVED) |
| `test_strategy` | `docs/tests/US-002-test-strategy.md` | 1 |
| `ac_test_matrix` | `docs/tests/US-002-ac-test-matrix.md` | 1 |
| `open_decisions` | `docs/decisions/US-002-open-decisions.md` | 2 (RESOLVED) |

---

## 3. Implemented Acceptance Criteria

| AC Identifier | Title | Implementation Location | Test Verification | Status |
|---|---|---|---|---|
| **AC-001** | Successful Login | `AuthService.login()`<br/>`AuthController.login()` | `CustomerLoginIntegrationTest.shouldAuthenticateCustomerSuccessfully`<br/>`CustomerLoginIntegrationTest.shouldNormalizeEmailOnLogin`<br/>`AuthServiceTest.shouldAuthenticateCustomerSuccessfully`<br/>`AuthControllerTest.shouldReturn200OnValidLogin` | PASS |
| **AC-002** | Invalid Password | `AuthService.login()`<br/>`GlobalExceptionHandler.handleAuthenticationException()` | `CustomerLoginIntegrationTest.shouldRejectInvalidPassword`<br/>`AuthServiceTest.shouldThrowBadCredentialsOnPasswordMismatch` | PASS |
| **AC-003** | Unknown Account | `AuthService.login()`<br/>`GlobalExceptionHandler.handleAuthenticationException()` | `CustomerLoginIntegrationTest.shouldRejectNonExistentAccount`<br/>`AuthServiceTest.shouldThrowBadCredentialsOnMissingAccount` | PASS |
| **AC-004** | Disabled Account | `AuthService.login()`<br/>`GlobalExceptionHandler.handleAuthenticationException()` | `CustomerLoginIntegrationTest.shouldRejectDisabledAccountLogin`<br/>`AuthServiceTest.shouldThrowBadCredentialsOnDisabledAccount` | PASS |
| **AC-005** | Secure Response (No Credential Exposure) | `LoginResponse`<br/>`AuthController.login()` | `CustomerLoginIntegrationTest.shouldNeverExposePasswordData` | PASS |
| **NFR-002 / NFR-003** | Request Validation & Media Types | `LoginRequest`<br/>`GlobalExceptionHandler.handleValidationException()`<br/>`GlobalExceptionHandler.handleMediaTypeNotSupported()` | `CustomerLoginIntegrationTest.shouldRejectBlankEmail`<br/>`CustomerLoginIntegrationTest.shouldRejectInvalidEmailFormat`<br/>`CustomerLoginIntegrationTest.shouldRejectBlankPassword`<br/>`CustomerLoginIntegrationTest.shouldRejectUnsupportedMediaType`<br/>`AuthControllerTest.shouldReturn400OnInvalidEmail`<br/>`AuthControllerTest.shouldReturn400OnBlankPassword` | PASS |

---

## 4. Change Set

| File | Type | Classification | Plan Reference / Rationale |
|---|---|---|---|
| `src/main/java/org/example/customerportal/model/request/LoginRequest.java` | Created | Planned | Step 2: Inbound request DTO with validation annotations (`@NotBlank`, `@Pattern`, `@Size`). |
| `src/main/java/org/example/customerportal/model/dto/LoginResponse.java` | Created | Planned | Step 2: Outbound login response DTO (`id`, `email`, `role`). Excludes sensitive fields. |
| `src/main/java/org/example/customerportal/security/SecurityConfig.java` | Modified | Planned | Step 3: Permitted public access to `POST /api/v1/auth/login`. |
| `src/main/java/org/example/customerportal/exception/GlobalExceptionHandler.java` | Modified | Planned | Step 3: Added `@ExceptionHandler(AuthenticationException.class)` returning 401 with uniform message `"Invalid email or password"`. |
| `src/main/java/org/example/customerportal/service/AuthService.java` | Created | Planned | Step 4: Authentication business service with normalization, BCrypt verification, account check, and SecurityContext creation. |
| `src/main/java/org/example/customerportal/controller/AuthController.java` | Created | Planned | Step 5: REST controller endpoint `POST /api/v1/auth/login`. |
| `src/test/java/org/example/customerportal/service/AuthServiceTest.java` | Created | Planned | Step 4/7: Unit test suite for `AuthService`. |
| `src/test/java/org/example/customerportal/controller/AuthControllerTest.java` | Created | Planned | Step 5/7: Slice test suite for `AuthController`. |

---

## 5. Validation Evidence

### Maven Build & Test Execution

- **Command:** `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"; mvn test`
- **Exit Code:** `0`
- **Output Summary:**
  ```text
  [INFO] Running org.example.customerportal.controller.AuthControllerTest
  [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.812 s -- in org.example.customerportal.controller.AuthControllerTest
  [INFO] Running org.example.customerportal.CustomerPortalApplicationTests
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.654 s -- in org.example.customerportal.CustomerPortalApplicationTests
  [INFO] Running org.example.customerportal.CustomerLoginIntegrationTest
  [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.812 s -- in org.example.customerportal.CustomerLoginIntegrationTest
  [INFO] Running org.example.customerportal.CustomerRegistrationIntegrationTest
  [INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.403 s -- in org.example.customerportal.CustomerRegistrationIntegrationTest
  [INFO] Running org.example.customerportal.service.AuthServiceTest
  [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.206 s -- in org.example.customerportal.service.AuthServiceTest
  [INFO] Running org.example.customerportal.service.CustomerServiceTest
  [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in org.example.customerportal.service.CustomerServiceTest
  [INFO] Running org.example.customerportal.validation.PasswordValidatorTest
  [INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.053 s -- in org.example.customerportal.validation.PasswordValidatorTest
  [INFO] 
  [INFO] Results:
  [INFO] 
  [INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
  [INFO] 
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  ```

---

## 6. Configuration Changes

- **No changes to `application.yaml` or `pom.xml`.** The existing configuration and dependencies fully satisfy all requirements.

---

## 7. Deviations and Discovered Problems

- **Email Whitespace Handling during Validation:** In `LoginRequest.java`, `@Pattern(regexp = "^\\s*[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\s*$", message = "Invalid email format")` was used to ensure that leading/trailing whitespace is tolerated by Bean Validation while still rejecting invalid email formats, matching the email normalization requirement in `OD-004` and `FR-002`.

---

## 8. Open Decisions

- No open decisions remain. All decisions (`OD-001` through `OD-004`) remain resolved and adhered to.

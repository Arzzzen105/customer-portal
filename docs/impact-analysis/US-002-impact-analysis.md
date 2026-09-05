---
artifact_type: impact_analysis
story: US-002
version: 1
status: DRAFT
created_at: 2026-09-02T13:30:00Z
updated_at: 2026-09-02T13:30:00Z
produced_by: impact-analyzer
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
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
semantic_analysis: TEXT_FALLBACK
---

# Impact Analysis — US-002: Customer Login

## 1. Executive Summary

- **Change Purpose:** Implement customer authentication (login), allowing registered customers to authenticate using their email and password, establishing an authenticated Spring Security session context and session cookie (`JSESSIONID`) to access protected portal resources.
- **Expected Scope:**
  - Introduce public authentication API endpoint (`POST /api/v1/auth/login`).
  - Introduce request payload DTO (`LoginRequest`) with Jakarta Bean Validation and response DTO (`LoginResponse`).
  - Introduce `AuthController` in `org.example.customerportal.controller` and `AuthService` in `org.example.customerportal.service`.
  - Update `SecurityConfig` to permit unauthenticated access to `POST /api/v1/auth/login` and establish session context.
  - Update `GlobalExceptionHandler` to handle authentication exceptions and return uniform `401 Unauthorized` anti-enumeration error bodies.
  - Reuse the existing persistence model (`customer` table, `Customer` entity, `CustomerRepository`) with zero database schema migrations.
  - Implement comprehensive automated test suite (Unit, WebMvc slice, and Integration tests).
- **Affected Architectural Areas:** Controller, Service, Request/Response DTOs, Security Configuration, Exception Handling, and Tests.
- **Overall Risk:** LOW. The changes build directly on the existing `US-001` customer persistence baseline without schema mutations, external dependency additions, or breaking changes to existing endpoints.

---

## 2. Source Artifacts

| Artifact Type | Path | Version / Status |
|---|---|---|
| `story` | `docs/stories/US-002-customer-login.md` | unversioned (input) |
| `specification` | `docs/specifications/US-002-spec.md` | 1 (APPROVED) |
| `specification_review` | `docs/reviews/specifications/US-002-spec-review.md` | 1 (APPROVED) |
| `api_design` | `docs/designs/api/US-002-api-design.md` | 1 (DRAFT) |
| `openapi` | `docs/designs/api/US-002-openapi.yaml` | 1 (DRAFT) |
| `database_design` | `docs/designs/database/US-002-db-design.md` | 1 (DRAFT) |
| `entity_model` | `docs/designs/database/US-002-entity-model.md` | 1 (DRAFT) |
| `design_review` | `docs/reviews/designs/US-002-design-review.md` | 1 (APPROVED) |
| `open_decisions` | `docs/decisions/US-002-open-decisions.md` | 2 (RESOLVED) |

---

## 3. Business Capability Impact

- **Customer Authentication:** Registered users can submit credentials to verify identity and obtain an authenticated session.
- **Anti-Enumeration Protection:** Authentication failures across non-existent email, invalid password, and disabled accounts return identical `HTTP 401 Unauthorized` responses (`"Invalid email or password"`).
- **Authorization Context:** Successful login establishes the user principal and assigned role (`ROLE_CUSTOMER` / `ROLE_ADMIN`) in the Spring `SecurityContext`.
- **Sensitive Data Isolation:** Plaintext passwords and BCrypt hashes are strictly excluded from API responses, headers, and logs.

---

## 4. Module Impact

| Module Name | Impact Type | Rationale | Confidence |
|---|---|---|---|
| `customer-portal` | Modify | Single-module Maven application. Adds new controller, service, DTOs, security rules, exception handlers, and tests. | HIGH |

---

## 5. Package Impact

| Package | Responsibility | Impact Type | Rationale | Architecture Constraint (`package-map.md`) |
|---|---|---|---|---|
| `controller` | HTTP endpoint routing & request mapping | Modify / Add | Introduce `AuthController` handling `POST /api/v1/auth/login`. | Depends only on `service`, `model.dto`, `model.request`. |
| `service` | Business logic & authentication orchestration | Modify / Add | Introduce `AuthService` (email normalization, credential verification, enabled check, session setup). | Owns `@Transactional`; depends on `repository`, `model.entity`, `model.dto`, `model.request`, `exception`, `security`. |
| `repository` | JPA repository interfaces | Reuse | Existing `CustomerRepository.findByEmail(String email)` is reused. | Queries only; depends only on `model.entity`. |
| `model.entity` | Domain persistence entity | Reuse | Existing `Customer` entity is reused. | Leaf package; never exposed directly to API. |
| `model.dto` | API response DTOs | Modify / Add | Introduce `LoginResponse` (`id`, `email`, `role`). | Leaf package; no credential fields. |
| `model.request` | Inbound request DTOs | Modify / Add | Introduce `LoginRequest` (`email`, `password`) with Bean Validation. | Bound with `@Valid` in controller. |
| `security` | Spring Security configuration | Modify | Update `SecurityConfig` to permit `POST /api/v1/auth/login`. | Follows `security-conventions.md`. |
| `exception` | Exception handling & advice | Modify | Update `GlobalExceptionHandler` to handle authentication failure exceptions (mapping to uniform 401). | Single `@RestControllerAdvice` mapping exceptions to AC-6 error JSON. |
| `config` | Infrastructure configuration | Reuse | Existing `JpaAuditingConfig` remains unchanged. | Framework only. |

---

## 6. Expected File Changes

### 6.1 Files To Create

| Expected File Path | Responsibility | Source Requirement | Confidence |
|---|---|---|---|
| `src/main/java/org/example/customerportal/controller/AuthController.java` | REST Controller exposing `POST /api/v1/auth/login` | FR-001, FR-007, §7.1 | HIGH |
| `src/main/java/org/example/customerportal/service/AuthService.java` | Business service authenticating credentials and establishing session context | FR-002, FR-003, FR-004, FR-006 | HIGH |
| `src/main/java/org/example/customerportal/model/request/LoginRequest.java` | Inbound login request DTO with Bean Validation annotations | FR-001, §5.1, §5.2 | HIGH |
| `src/main/java/org/example/customerportal/model/dto/LoginResponse.java` | API success response DTO returning authenticated customer identity | FR-007, AC-005, OD-003 | HIGH |
| `src/test/java/org/example/customerportal/controller/AuthControllerTest.java` | Controller web slice tests (`@WebMvcTest`) for login endpoint | NFR-005, AC-001–AC-005 | HIGH |
| `src/test/java/org/example/customerportal/service/AuthServiceTest.java` | Unit tests for authentication business logic and failure paths | NFR-005, AC-001–AC-004 | HIGH |
| `src/test/java/org/example/customerportal/CustomerLoginIntegrationTest.java` | End-to-end integration tests (`@SpringBootTest`) for login and session establishment | NFR-005, AC-001–AC-005 | HIGH |

### 6.2 Files To Modify

| Expected File Path | Responsibility | Reason | Confidence |
|---|---|---|---|
| `src/main/java/org/example/customerportal/security/SecurityConfig.java` | Security filter chain configuration | Permit unauthenticated access to `POST /api/v1/auth/login` (`SC-4`, `SEC-001`) | HIGH |
| `src/main/java/org/example/customerportal/exception/GlobalExceptionHandler.java` | Centralized exception handler | Map authentication exceptions (`BadCredentialsException` / `AuthenticationException`) to uniform `401 Unauthorized` per AC-6 / OD-002 | HIGH |

### 6.3 Files To Reuse

| File Path | Responsibility |
|---|---|
| `src/main/java/org/example/customerportal/model/entity/Customer.java` | Persistent Customer entity. |
| `src/main/java/org/example/customerportal/repository/CustomerRepository.java` | Customer persistence repository with `findByEmail`. |
| `src/main/java/org/example/customerportal/model/dto/ErrorResponse.java` | Standard error body DTO adhering to `AC-6`. |
| `src/main/java/org/example/customerportal/model/dto/FieldErrorDto.java` | Field error DTO for validation responses. |
| `src/main/java/org/example/customerportal/config/JpaAuditingConfig.java` | JPA auditing configuration. |
| `src/main/java/org/example/customerportal/CustomerPortalApplication.java` | Main application entry point. |
| `src/main/resources/schema.sql` | Relational database DDL. |
| `src/main/resources/application.yaml` | Application configuration. |
| `pom.xml` | Maven dependencies and build plugins. |

### 6.4 Files Potentially Affected

- None outside the scoped authentication feature.

---

## 7. API Impact

- **New Endpoint:** `POST /api/v1/auth/login`
- **Request Format:** `Content-Type: application/json`
  ```json
  {
    "email": "customer@example.com",
    "password": "Password123!"
  }
  ```
- **Response Format:** `200 OK`, `Set-Cookie: JSESSIONID=...`
  ```json
  {
    "id": 1,
    "email": "customer@example.com",
    "role": "CUSTOMER"
  }
  ```
- **Error Responses:**
  - `400 Bad Request`: Validation failure (blank email, invalid email format, blank password) or malformed JSON with `fieldErrors`.
  - `401 Unauthorized`: Authentication failure (invalid password, unknown account, disabled account) with uniform message `"Invalid email or password"`.
  - `415 Unsupported Media Type`: Non-JSON request body.
  - `500 Internal Server Error`: Generic unhandled exception.
- **Backward Compatibility:** Completely non-breaking; introduces new endpoint without affecting existing registration endpoint `/api/v1/customers`.

---

## 8. Persistence Impact

- **Database Engine:** H2 (file-based locally; in-memory isolated for tests per `PC-1`).
- **Schema Changes:** NONE. Reuses table `customer` defined in `src/main/resources/schema.sql`.
- **Entity Model:** Reuses `Customer` entity (`id`, `email`, `passwordHash`, `role`, `enabled`, `createdAt`, `updatedAt`).
- **Repository Methods:** Reuses `CustomerRepository.findByEmail(String email)` querying over index `uq_customer_email`.
- **Read-Only Operation:** Authentication flow performs read queries and does not mutate persistent database state.

---

## 9. Security Impact

- **Public Access Configuration:** `POST /api/v1/auth/login` configured in `SecurityConfig` with `permitAll()`.
- **Password Verification:** Verification executed using `BCryptPasswordEncoder.matches(rawPassword, storedHash)` (`SC-1`, `SEC-002`). Plaintext comparisons are strictly forbidden.
- **Session Management:** Upon successful login, Spring Security session context is established and associated with an `HttpOnly` `JSESSIONID` cookie (`SC-3`, `SEC-005`).
- **Anti-Enumeration:** Uniform `401 Unauthorized` with generic message `"Invalid email or password"` returned for non-existent users, bad passwords, and disabled accounts (`SC-3`, `OD-002`).
- **Credential Protection:** Plaintext passwords exist only in memory during request execution; passwords and hashes are omitted from all response DTOs and logs (`SC-1`, `SC-9`, `AD-4`).

---

## 10. Testing Impact

- **Unit Tests:**
  - `AuthServiceTest`: Test successful authentication, password mismatch, non-existent email, disabled account, email normalization.
- **WebMvc Slice Tests:**
  - `AuthControllerTest`: Test `200 OK` on valid credentials, `400 Bad Request` on invalid email/password formats, `415 Unsupported Media Type`.
- **Integration Tests:**
  - `CustomerLoginIntegrationTest`: Test end-to-end HTTP authentication flow, session cookie emission, anti-enumeration `401` errors, disabled account rejection, and response credential sanitation.

---

## 11. Configuration and Dependency Impact

- **Dependencies:** No new dependencies required. Spring Boot Starter Security, WebMvc, Validation, and Data JPA are already present in `pom.xml`.
- **Application Properties:** Existing `application.yaml` configurations for H2 database, SQL init, and JPA validation remain sufficient.

---

## 12. Documentation Impact

- No documentation updates required outside generated workflow artifacts.

---

## 13. Risks

| Risk ID | Severity | Description | Affected Area | Mitigation | Human Decision Required |
|---|---|---|---|---|---|
| **RSK-001** | Minor | User enumeration vulnerability through timing or error message differences | Security | Enforce uniform `401 Unauthorized` response with `"Invalid email or password"` across all failure branches per `OD-002` and `SC-3`. | No (pre-resolved in OD-002) |
| **RSK-002** | Minor | Session context / cookie not properly attached on login | Security / API | Verify Spring Security `SecurityContextRepository` / `HttpSession` handling in integration tests. | No |

---

## 14. Open Decisions

No blocking Open Decisions were identified. All Open Decisions (`OD-001` through `OD-004`) were resolved with Option A.

---

## 15. Planning Inputs

Key facts for `implementation-planner`:
1. Update `SecurityConfig` to add `POST /api/v1/auth/login` to `permitAll()`.
2. Implement `LoginRequest` with `@NotBlank`, `@Email`, `@Size(max=255)` on `email`, and `@NotBlank`, `@Size(max=72)` on `password`.
3. Implement `LoginResponse` with `id`, `email`, and `role`.
4. Implement `AuthService` to normalize email (`trim().toLowerCase()`), query `CustomerRepository.findByEmail`, check `customer.isEnabled()`, match BCrypt password via `PasswordEncoder`, and populate `SecurityContextHolder`.
5. Implement `AuthController` with `POST /api/v1/auth/login` delegating to `AuthService` and returning `ResponseEntity.ok(response)`.
6. Update `GlobalExceptionHandler` to handle `AuthenticationException` / `BadCredentialsException` returning `401 Unauthorized` with structured error body (`message: "Invalid email or password"`).
7. Implement unit tests (`AuthServiceTest`, `AuthControllerTest`) and integration tests (`CustomerLoginIntegrationTest`).

---

## 16. Traceability

| Acceptance Criterion | Specification Section | Design Artifact | Affected System Area | Test Category |
|---|---|---|---|---|
| **AC-001 (Successful Login)** | §3 FR-001, FR-002, FR-003, FR-006, FR-007 | `api-design.md`, `openapi.yaml`, `entity-model.md` | Controller, Service, Security, DTO | Integration & WebMvc |
| **AC-002 (Invalid Password)** | §3 FR-003, FR-005, §7.2 | `api-design.md`, `openapi.yaml` | Service, ExceptionHandler | Integration & Unit |
| **AC-003 (Unknown Account)** | §3 FR-002, FR-005, §7.2 | `api-design.md`, `openapi.yaml` | Service, ExceptionHandler | Integration & Unit |
| **AC-004 (Disabled Account)** | §3 FR-004, FR-005, §7.2 | `api-design.md`, `db-design.md` | Service, ExceptionHandler | Integration & Unit |
| **AC-005 (Secure Response)** | §3 FR-007, §6 SEC-003 | `api-design.md`, `entity-model.md` | DTO, Controller | Integration & WebMvc |

---

## 17. Analysis Limitations

- Semantic code analysis via IntelliJ IDEA MCP was unavailable; analysis was performed via built-in repository inspection (`TEXT_FALLBACK`). High confidence is maintained due to concise, well-structured codebase.

---

## 18. Readiness Result

**`PASS`** — The change surface for US-002 is fully identified with high confidence, no schema migrations or new dependencies are needed, all security and anti-enumeration constraints are addressed, and the active Story is ready for `IMPLEMENTATION_PLANNING`.

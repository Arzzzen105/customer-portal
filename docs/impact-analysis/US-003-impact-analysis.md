---
artifact_type: impact_analysis
story: US-003
version: 1
status: DRAFT
created_at: 2026-09-05T10:11:00Z
updated_at: 2026-09-05T10:11:00Z
produced_by: impact-analyzer
inputs:
  - path: docs/stories/US-003-customer-profile-view.md
    version: null
  - path: docs/specifications/US-003-spec.md
    version: 1
  - path: docs/reviews/specifications/US-003-spec-review.md
    version: 1
  - path: docs/designs/api/US-003-api-design.md
    version: 1
  - path: docs/designs/api/US-003-openapi.yaml
    version: 1
  - path: docs/designs/database/US-003-db-design.md
    version: 1
  - path: docs/designs/database/US-003-entity-model.md
    version: 1
  - path: docs/reviews/designs/US-003-design-review.md
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
    version: 2
supersedes: null
semantic_analysis: TEXT_FALLBACK
---

# Impact Analysis — US-003: View Customer Profile

## 1. Executive Summary

- **Change Purpose:** Implement customer profile retrieval (`GET /api/v1/customers/{id}`), allowing authenticated customers to inspect their own profile information while strictly enforcing tenant isolation and ownership validation to prevent horizontal privilege escalation, and ensuring sensitive credentials (`passwordHash`) are excluded from responses.
- **Expected Scope:**
  - Add `GET /api/v1/customers/{id}` endpoint to existing `CustomerController`.
  - Add `getCustomerProfile(Long id, Authentication authentication)` to `CustomerService` with ownership verification and read-only transaction semantics.
  - Introduce domain exception `CustomerNotFoundException` in `org.example.customerportal.exception`.
  - Update `GlobalExceptionHandler` to handle `CustomerNotFoundException` (404), `AccessDeniedException` (403), and `MethodArgumentTypeMismatchException` (400) conforming to `api-conventions.md` AC-6 error format.
  - Update `SecurityConfig` with an `AuthenticationEntryPoint` to return standard AC-6 error JSON on unauthenticated requests (401).
  - Reuse the existing persistence model (`customer` table, `Customer` entity, `CustomerRepository`) with zero database migrations.
  - Reuse existing `CustomerResponse` DTO (`id`, `email`, `role`, `createdAt`).
  - Implement comprehensive unit, web slice, and integration test coverage.
- **Affected Architectural Areas:** Controller, Service, Exception Handling, Security Configuration, and Automated Tests.
- **Overall Risk:** LOW. The change is a read-only addition building upon the established `US-001` and `US-002` foundation without schema migrations, third-party dependency additions, or breaking changes to existing endpoints.

---

## 2. Source Artifacts

| Artifact Type | Path | Version / Status |
|---|---|---|
| `story` | `docs/stories/US-003-customer-profile-view.md` | unversioned (input) |
| `specification` | `docs/specifications/US-003-spec.md` | 1 (APPROVED) |
| `specification_review` | `docs/reviews/specifications/US-003-spec-review.md` | 1 (APPROVED) |
| `api_design` | `docs/designs/api/US-003-api-design.md` | 1 (DRAFT) |
| `openapi` | `docs/designs/api/US-003-openapi.yaml` | 1 (DRAFT) |
| `database_design` | `docs/designs/database/US-003-db-design.md` | 1 (DRAFT) |
| `entity_model` | `docs/designs/database/US-003-entity-model.md` | 1 (DRAFT) |
| `design_review` | `docs/reviews/designs/US-003-design-review.md` | 1 (APPROVED) |
| `open_decisions` | `docs/decisions/US-003-open-decisions.md` | 2 (APPROVED) |

---

## 3. Business Capability Impact

- **Customer Profile Self-Service:** Authenticated customers can retrieve their registered profile details (`id`, `email`, `role`, `createdAt`).
- **Resource Ownership & Tenant Isolation:** Strict service-layer verification ensures that customers can only view their own profile. Attempts to view another customer's ID are denied with `HTTP 403 Forbidden` (`OD-002` Option A, `AC-002`).
- **Pre-Query Ownership Verification:** Access validation precedes database entity lookup to avoid timing side-channels and resource existence probing (`OD-004` Option A).
- **Credential & Internal State Protection:** Plaintext passwords, password hashes, and internal flags (`enabled`, `updatedAt`) are excluded from profile responses (`AC-003`, `OD-003` Option A).

---

## 4. Module Impact

| Module Name | Impact Type | Rationale | Confidence |
|---|---|---|---|
| `customer-portal` | Modify | Single-module Maven application. Adds new endpoint handler, service retrieval logic, exception handling, and tests. | HIGH |

---

## 5. Package Impact

| Package | Responsibility | Impact Type | Rationale | Architecture Constraint (`package-map.md`) |
|---|---|---|---|---|
| `controller` | HTTP endpoint routing & request handling | Modify | Add `GET /api/v1/customers/{id}` handler to `CustomerController`. | Depends only on `service`, `model.dto`, `model.request`. No entities in signature. |
| `service` | Business logic, ownership verification & DTO mapping | Modify | Add `getCustomerProfile(Long id, Authentication auth)` to `CustomerService`. | Owns `@Transactional(readOnly = true)`. Depends on `repository`, `model.entity`, `model.dto`, `exception`. |
| `repository` | JPA repository interfaces | Reuse | Reuse inherited `CustomerRepository.findById(Long id)` and `findByEmail(String email)`. | Queries only. Depends only on `model.entity`. |
| `model.entity` | Domain persistence entities | Reuse | Reuse existing `Customer` entity. | Leaf package. Never exposed directly to API. |
| `model.dto` | API response DTOs | Reuse | Reuse `CustomerResponse` (`id`, `email`, `role`, `createdAt`) and `ErrorResponse`. | Leaf package. Excludes all credentials. |
| `security` | Spring Security configuration & entry points | Modify | Configure `AuthenticationEntryPoint` in `SecurityConfig` to return AC-6 401 error envelope on unauthenticated access. | Follows `security-conventions.md`. |
| `exception` | Exception definitions & centralized advice | Modify / Add | Add `CustomerNotFoundException`; update `GlobalExceptionHandler` for 403, 404, and 400 type mismatch. | Single `@RestControllerAdvice` mapping exceptions to AC-6 error JSON. |
| `config` | Infrastructure configuration | Reuse | Existing `JpaAuditingConfig` remains unchanged. | Framework only. |

---

## 6. Expected File Changes

### 6.1 Files To Create

| Expected File Path | Responsibility | Source Requirement | Confidence |
|---|---|---|---|
| `src/main/java/org/example/customerportal/exception/CustomerNotFoundException.java` | Domain exception thrown when customer record does not exist for caller's ID | FR-005, §7.2, AC-5 | HIGH |
| `src/test/java/org/example/customerportal/CustomerProfileIntegrationTest.java` | End-to-end integration tests (`@SpringBootTest`) for profile retrieval, ownership enforcement, credential exclusion, and error handling | NFR-005, AC-001–AC-004 | HIGH |

### 6.2 Files To Modify

| Expected File Path | Responsibility | Reason | Confidence |
|---|---|---|---|
| `src/main/java/org/example/customerportal/controller/CustomerController.java` | REST Controller | Add `GET /{id}` method delegating to `CustomerService` and returning `CustomerResponse` | HIGH |
| `src/main/java/org/example/customerportal/service/CustomerService.java` | Business Service | Add `getCustomerProfile(Long id, Authentication auth)` enforcing ownership before query | HIGH |
| `src/main/java/org/example/customerportal/exception/GlobalExceptionHandler.java` | Centralized Exception Handler | Map `AccessDeniedException` (403), `CustomerNotFoundException` (404), and `MethodArgumentTypeMismatchException` (400) to AC-6 error JSON | HIGH |
| `src/main/java/org/example/customerportal/security/SecurityConfig.java` | Security Configuration | Add `AuthenticationEntryPoint` to emit AC-6 compliant 401 error JSON on unauthenticated requests | HIGH |
| `src/test/java/org/example/customerportal/controller/CustomerControllerTest.java` | Web Slice Tests | Add controller tests for `GET /api/v1/customers/{id}` (200 OK, 400 Bad Request) | HIGH |
| `src/test/java/org/example/customerportal/service/CustomerServiceTest.java` | Unit Tests | Add unit tests for ownership validation, successful profile return, not found, and credential exclusion | HIGH |

### 6.3 Files To Reuse

| File Path | Responsibility |
|---|---|
| `src/main/java/org/example/customerportal/model/entity/Customer.java` | Persistent Customer entity. |
| `src/main/java/org/example/customerportal/repository/CustomerRepository.java` | Spring Data repository with `findById` and `findByEmail`. |
| `src/main/java/org/example/customerportal/model/dto/CustomerResponse.java` | API response DTO with `{ id, email, role, createdAt }`. |
| `src/main/java/org/example/customerportal/model/dto/ErrorResponse.java` | Standard error body DTO adhering to AC-6. |
| `src/main/resources/schema.sql` | Relational database schema definition (no changes needed). |
| `src/main/resources/application.yaml` | Application configuration. |
| `pom.xml` | Maven build dependencies. |

### 6.4 Files Potentially Affected

- None outside the customer profile feature scope.

---

## 7. API Impact

- **New Operation:** `GET /api/v1/customers/{id}`
- **Security Requirement:** `cookieAuth` session authentication required (`SC-3`, `SC-4`).
- **Path Parameter:** `id` (`Long` / `int64`, `minimum: 1`).
- **Success Response:** `HTTP 200 OK`
  ```json
  {
    "id": 1,
    "email": "customer@example.com",
    "role": "CUSTOMER",
    "createdAt": "2026-09-05T10:00:00Z"
  }
  ```
- **Error Responses (Conforming to `api-conventions.md` AC-6):**
  - `400 Bad Request`: Non-numeric `id` parameter (e.g. `/api/v1/customers/abc`) or invalid type conversion.
  - `401 Unauthorized`: Unauthenticated request (no active session cookie).
  - `403 Forbidden`: Ownership mismatch (authenticated user ID $\neq$ path variable `{id}`) with message `"Access denied"`.
  - `404 Not Found`: Caller's own customer ID not found in database with message `"Customer not found with id: {id}"`.
  - `500 Internal Server Error`: Unhandled server exceptions.
- **Backward Compatibility:** Preserves existing endpoints `POST /api/v1/customers` and `POST /api/v1/auth/login` without breaking changes.

---

## 8. Persistence Impact

- **Database Engine:** H2 (file-based locally; in-memory isolated for tests per `PC-1`).
- **Schema Migrations:** NONE. Reuses existing `customer` table defined in `src/main/resources/schema.sql`.
- **Entity Model:** Reuses existing `Customer` entity (`id`, `email`, `passwordHash`, `role`, `enabled`, `createdAt`, `updatedAt`).
- **Repository Operations:** Inherited `findById(Long id)` executes against clustered primary key index `pk_customer` with $O(1)$ complexity.
- **Transaction Semantics:** Read-only transaction (`@Transactional(readOnly = true)`) ensures zero database modifications.

---

## 9. Security Impact

- **Protected Endpoint Access:** `GET /api/v1/customers/{id}` is protected by default `anyRequest().authenticated()` (`SC-4`, `SEC-001`).
- **Horizontal Privilege Escalation Prevention:** Strict service-layer comparison between authenticated caller identity and requested `{id}` guarantees customer data isolation (`SC-4`, `SEC-002`, `AC-002`).
- **Anti-Probing Precedence:** Ownership check occurs prior to querying the database, preventing enumeration of customer IDs via timing or response differences (`OD-004` Option A).
- **Credential Secrecy:** Neither plaintext passwords nor `passwordHash` are mapped to `CustomerResponse` or exposed in error messages (`SC-1`, `SC-9`, `AC-003`).
- **Error Hygiene:** All error responses adhere to AC-6 without leaking database details, class names, or stack traces (`SC-9`, `SEC-005`).

---

## 10. Testing Impact

- **Unit Tests (`CustomerServiceTest`):**
  - Successful profile retrieval when caller owns requested ID.
  - Ownership mismatch throws `AccessDeniedException`.
  - Caller owns ID but record missing throws `CustomerNotFoundException`.
  - Response DTO excludes `passwordHash` and internal flags.
- **Web Slice Tests (`CustomerControllerTest`):**
  - `GET /api/v1/customers/{id}` returns `200 OK` with JSON matching `CustomerResponse`.
  - Non-numeric ID returns `400 Bad Request`.
- **Integration Tests (`CustomerProfileIntegrationTest`):**
  - End-to-end self-profile retrieval with authenticated session (`AC-001`).
  - Cross-customer access attempts return `403 Forbidden` (`AC-002`).
  - Response payload verification confirming complete omission of password fields (`AC-003`).
  - Verification of AC-6 error structure across `400`, `401`, `403`, and `404` scenarios (`AC-004`).
  - Unauthenticated access returns `401 Unauthorized`.

---

## 11. Configuration and Dependency Impact

- **Dependencies:** No new Maven dependencies required. Existing Spring Boot Web, Security, Validation, and JPA starters are sufficient.
- **Application Configuration:** Existing `application.yaml` settings remain sufficient.

---

## 12. Documentation Impact

- No external documentation updates required outside generated workflow artifacts.

---

## 13. Risks

| Risk ID | Severity | Description | Affected Area | Mitigation | Human Decision Required |
|---|---|---|---|---|---|
| **RSK-001** | Minor | Unauthenticated requests return default Spring Security error instead of AC-6 envelope | Security / API | Configure custom `AuthenticationEntryPoint` in `SecurityConfig` to serialize standard `ErrorResponse`. | No (conforms to AC-6 & SEC-001) |
| **RSK-002** | Minor | Type mismatch on non-numeric path parameter defaults to 500 error | Exception Handling | Add `@ExceptionHandler(MethodArgumentTypeMismatchException.class)` in `GlobalExceptionHandler` to return `400 Bad Request`. | No (conforms to AC-5 / AC-6) |

---

## 14. Open Decisions

No blocking Open Decisions were identified. All Open Decisions (`OD-001` through `OD-004`) were resolved with human approval.

---

## 15. Planning Inputs

Key facts for `implementation-planner`:
1. Add `CustomerNotFoundException` in `org.example.customerportal.exception`.
2. Update `GlobalExceptionHandler` with handlers for `AccessDeniedException` (403), `CustomerNotFoundException` (404), and `MethodArgumentTypeMismatchException` (400).
3. Update `SecurityConfig` to configure an `AuthenticationEntryPoint` writing an AC-6 `ErrorResponse` for unauthenticated requests (401).
4. Implement `getCustomerProfile(Long id, Authentication authentication)` in `CustomerService` with `@Transactional(readOnly = true)`:
   - Extract authenticated principal identity from `Authentication`.
   - Verify that the authenticated customer's ID equals requested `id`; if mismatch, throw `AccessDeniedException("Access denied")`.
   - Query `customerRepository.findById(id)`; if empty, throw `CustomerNotFoundException("Customer not found with id: " + id)`.
   - Map `Customer` to `CustomerResponse` (`id`, `email`, `role`, `createdAt`).
5. Add `GET /{id}` in `CustomerController` delegating to `CustomerService`.
6. Add unit tests in `CustomerServiceTest`, slice tests in `CustomerControllerTest`, and integration tests in `CustomerProfileIntegrationTest`.

---

## 16. Traceability

| Acceptance Criterion | Specification Section | Design Artifact | Affected System Area | Test Category |
|---|---|---|---|---|
| **AC-001 (View Own Profile)** | §3 FR-001, FR-002, FR-003, FR-007 | `api-design.md`, `openapi.yaml`, `entity-model.md` | Controller, Service, DTO | Integration, Slice, Unit |
| **AC-002 (Ownership Enforcement)** | §3 FR-003, FR-004, §6 SEC-002, SEC-003 | `api-design.md`, `openapi.yaml` | Service, ExceptionHandler | Integration, Unit |
| **AC-003 (Sensitive Data Exclusion)** | §3 FR-006, FR-007, §6 SEC-004 | `api-design.md`, `openapi.yaml`, `entity-model.md` | Service, DTO | Integration, Unit |
| **AC-004 (Consistent Response)** | §3 FR-001, FR-004, FR-005, §7.2 | `api-design.md`, `openapi.yaml` | Controller, ExceptionHandler, SecurityConfig | Integration, Slice |

---

## 17. Analysis Limitations

- Semantic code analysis via IntelliJ IDEA MCP was unavailable; analysis was performed via built-in repository inspection (`TEXT_FALLBACK`). High confidence is maintained due to the focused scope and clean separation of concerns in the codebase.

---

## 18. Readiness Result

**`PASS`** — The expected change surface for US-003 is comprehensively identified with high confidence. No database migrations, new dependencies, or breaking API changes are required. The active Story is fully ready for `IMPLEMENTATION_PLANNING`.

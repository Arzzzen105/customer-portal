---
artifact_type: impact_analysis
story: US-001
version: 1
status: DRAFT
created_at: 2026-08-31T16:00:30Z
updated_at: 2026-08-31T16:00:30Z
produced_by: impact-analyzer
inputs:
  - path: docs/specifications/US-001-spec.md
    version: 1
  - path: docs/reviews/specifications/US-001-spec-review.md
    version: 1
  - path: docs/designs/api/US-001-api-design.md
    version: 1
  - path: docs/designs/api/US-001-openapi.yaml
    version: 1
  - path: docs/designs/database/US-001-db-design.md
    version: 1
  - path: docs/designs/database/US-001-entity-model.md
    version: 1
  - path: docs/reviews/designs/US-001-design-review.md
    version: 1
  - path: docs/decisions/US-001-open-decisions.md
    version: 2
supersedes: null
semantic_analysis: TEXT_FALLBACK
---

# Impact Analysis — US-001: Customer Registration

## 1. Executive Summary

- **Change Purpose:** Implement self-service customer registration allowing new users to create accounts with email and password, establishing their identity in the Customer Portal with role `CUSTOMER`.
- **Expected Scope:** Introduction of customer registration API endpoint (`POST /api/v1/customers`), layered service logic, persistence schema (`customer` table), password hashing (`BCrypt`), Bean Validation, custom password complexity constraints, global exception handling, and comprehensive automated test suites.
- **Affected Architectural Areas:** Controller, Service, Repository, Entity, DTO/Request, Validation, Security, Configuration, Exception Handling, Database Schema, and POM Dependencies.
- **Overall Risk:** LOW. The changes introduce foundational baseline capabilities into an unencumbered greenfield codebase without backward compatibility risks.

---

## 2. Source Artifacts

| Artifact Type | Path | Version / Status |
|---|---|---|
| `story` | `docs/stories/US-001-register-customer.md` | unversioned (input) |
| `specification` | `docs/specifications/US-001-spec.md` | 1 (APPROVED) |
| `specification_review` | `docs/reviews/specifications/US-001-spec-review.md` | 1 (APPROVED) |
| `api_design` | `docs/designs/api/US-001-api-design.md` | 1 (DRAFT) |
| `openapi` | `docs/designs/api/US-001-openapi.yaml` | 1 (DRAFT) |
| `database_design` | `docs/designs/database/US-001-db-design.md` | 1 (DRAFT) |
| `entity_model` | `docs/designs/database/US-001-entity-model.md` | 1 (DRAFT) |
| `design_review` | `docs/reviews/designs/US-001-design-review.md` | 1 (APPROVED) |
| `open_decisions` | `docs/decisions/US-001-open-decisions.md` | 2 (RESOLVED) |

---

## 3. Business Capability Impact

- **New Capability:** Self-service registration (`POST /api/v1/customers`).
- **Identity & Access Management:** Default `CUSTOMER` role assignment (`ROLE_CUSTOMER`), enabled account activation.
- **Data Protection:** BCrypt password hashing before persistence; strict suppression of credentials from API responses and logs.

---

## 4. Module Impact

| Module Name | Impact Type | Rationale | Confidence |
|---|---|---|---|
| `customer-portal` | Modify | Single-module Maven application. Requires adding production classes, schema, config, and dependencies. | HIGH |

---

## 5. Package Impact

| Package | Responsibility | Impact Type | Rationale | Architecture Constraint (`package-map.md`) |
|---|---|---|---|---|
| `controller` | HTTP endpoint mapping | Create | `CustomerController` for `POST /api/v1/customers` | Depends only on `service`, `model.dto`, `model.request` |
| `service` | Business logic & orchestration | Create | `CustomerService` (normalization, duplicate check, BCrypt, mapping) | Owns `@Transactional`; no servlet types |
| `repository` | JPA repository | Create | `CustomerRepository` (`existsByEmail`, `findByEmail`) | Queries only; depends only on `model.entity` |
| `model.entity` | Domain persistence | Create | `Customer` entity | Leaf package; never exposed to API |
| `model.dto` | Response DTOs | Create | `CustomerResponse`, `FieldErrorDto` | Leaf package; no credential fields |
| `model.request` | Inbound request DTOs | Create | `RegisterCustomerRequest` | Validated with `@Valid` |
| `validation` | Custom validation rules | Create | `@ValidPassword` annotation and `PasswordValidator` | Independent validation logic |
| `security` | Security config & beans | Create | `SecurityConfig` (permits public endpoint) & `PasswordEncoder` | Security conventions SC-1, SC-4 |
| `config` | Infrastructure config | Create | `JpaAuditingConfig` for UTC auditing | Framework only |
| `exception` | Exception hierarchy & advice | Create | `DuplicateEmailException`, `GlobalExceptionHandler` | Single `@RestControllerAdvice` |

---

## 6. Expected File Changes

### 6.1 Files To Create

| Expected File Path | Responsibility | Source Requirement | Confidence |
|---|---|---|---|
| `src/main/java/org/example/customerportal/controller/CustomerController.java` | REST Controller exposing `POST /api/v1/customers` | FR-001, FR-007 | HIGH |
| `src/main/java/org/example/customerportal/service/CustomerService.java` | Business service interface and implementation for customer registration | FR-002, FR-003, FR-004, FR-005 | HIGH |
| `src/main/java/org/example/customerportal/repository/CustomerRepository.java` | Spring Data JPA repository for Customer entity | FR-002, FR-003, FR-005 | HIGH |
| `src/main/java/org/example/customerportal/model/entity/Customer.java` | JPA Entity mapped to `customer` table | §8, PC-3, PC-4, PC-5, PC-6 | HIGH |
| `src/main/java/org/example/customerportal/model/request/RegisterCustomerRequest.java` | Inbound request payload DTO with Bean Validation | FR-001, §5.1, §5.2 | HIGH |
| `src/main/java/org/example/customerportal/model/dto/CustomerResponse.java` | API success response DTO (no credentials) | FR-007, AC-005, OD-003 | HIGH |
| `src/main/java/org/example/customerportal/validation/ValidPassword.java` | Custom constraint annotation for password complexity | §5.2, SC-1 | HIGH |
| `src/main/java/org/example/customerportal/validation/PasswordValidator.java` | ConstraintValidator enforcing password complexity regex | §5.2, SC-1 | HIGH |
| `src/main/java/org/example/customerportal/security/SecurityConfig.java` | Security filter chain permitting public registration & password encoder bean | SEC-001, SEC-002, SC-1, SC-4 | HIGH |
| `src/main/java/org/example/customerportal/config/JpaAuditingConfig.java` | Enables JPA auditing for UTC timestamps | FR-006, PC-6 | HIGH |
| `src/main/java/org/example/customerportal/exception/DuplicateEmailException.java` | Domain exception thrown on duplicate email registration | FR-003, AC-002 | HIGH |
| `src/main/java/org/example/customerportal/exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` mapping exceptions to AC-6 error JSON | §7.2, AD-6, AC-6 | HIGH |
| `src/main/resources/schema.sql` | Explicit DDL for `customer` table and constraints | §8, PC-2, PC-5 | HIGH |
| `src/test/java/org/example/customerportal/controller/CustomerControllerTest.java` | Controller web slice tests (`@WebMvcTest`) | NFR-005, AC-001–AC-005 | HIGH |
| `src/test/java/org/example/customerportal/service/CustomerServiceTest.java` | Unit tests for service layer business logic | NFR-005, AC-001–AC-004 | HIGH |
| `src/test/java/org/example/customerportal/repository/CustomerRepositoryTest.java` | Persistence slice tests (`@DataJpaTest`) | NFR-004, NFR-005 | HIGH |
| `src/test/java/org/example/customerportal/validation/PasswordValidatorTest.java` | Unit tests for password validation rules | §5.2, NFR-005 | HIGH |
| `src/test/java/org/example/customerportal/CustomerRegistrationIntegrationTest.java` | End-to-end integration tests (`@SpringBootTest`) | NFR-005, AC-001–AC-005 | HIGH |

### 6.2 Files To Modify

| Expected File Path | Responsibility | Reason | Confidence |
|---|---|---|---|
| `pom.xml` | Maven build definition | Add `spring-boot-starter-validation` dependency per AD-5 | HIGH |
| `src/main/resources/application.yaml` | Application configuration | Configure H2 file database, SQL init mode (`always`), JPA `ddl-auto: validate` | HIGH |

### 6.3 Files To Reuse
- `src/main/java/org/example/customerportal/CustomerPortalApplication.java` — Main application runner.

### 6.4 Files Potentially Affected
- None outside the scoped customer feature.

---

## 7. API Impact

- **New Operation:** `POST /api/v1/customers`
- **Request Body:** JSON `{"email": "...", "password": "..."}`
- **Success Status:** `201 Created` with `Location: /api/v1/customers/{id}` and JSON body `{"id": 1, "email": "...", "role": "CUSTOMER", "createdAt": "..."}`
- **Error Responses:**
  - `400 Bad Request` with `fieldErrors` array for invalid email or password format
  - `409 Conflict` for duplicate email
  - `415 Unsupported Media Type` for non-JSON requests
  - `500 Internal Server Error` for unhandled exceptions

---

## 8. Persistence Impact

- **Table:** `customer`
- **Fields:** `id`, `email`, `password_hash`, `role`, `enabled`, `created_at`, `updated_at`
- **Constraints:** `pk_customer` on `id`, `uq_customer_email` on `email`
- **DDL:** Explicit in `src/main/resources/schema.sql`; Hibernate `ddl-auto=validate`.

---

## 9. Security Impact

- **Public Endpoint:** `POST /api/v1/customers` configured in `SecurityFilterChain` with `permitAll()`.
- **Password Encoder:** `BCryptPasswordEncoder` bean configured in `SecurityConfig`.
- **Sensitive Data:** Plaintext password never stored; password hash never returned in response DTOs or logs.

---

## 10. Testing Impact

- **Unit Tests:** `CustomerServiceTest`, `PasswordValidatorTest`
- **Web MVC Slicing:** `CustomerControllerTest` verifying status codes `201`, `400`, `409`, `415`
- **Data JPA Slicing:** `CustomerRepositoryTest` verifying uniqueness constraint and JPA auditing
- **Integration Tests:** `CustomerRegistrationIntegrationTest` verifying end-to-end HTTP registration, database record creation, and password hash verification.

---

## 11. Configuration and Dependency Impact

- **Dependencies:** Add `org.springframework.boot:spring-boot-starter-validation` to `pom.xml` (explicitly mandated by `architecture.md` AD-5 for Bean Validation).
- **Application Config:** Set H2 file database connection, `spring.sql.init.mode=always`, and `spring.jpa.hibernate.ddl-auto=validate`.

---

## 12. Documentation Impact

- No additional documentation updates required outside generated story artifacts.

---

## 13. Risks

| Risk ID | Severity | Description | Affected Area | Mitigation | Human Decision Required |
|---|---|---|---|---|---|
| **RSK-001** | Minor | Adding `spring-boot-starter-validation` dependency to `pom.xml` | Dependencies | Mandated by `architecture.md` AD-5; standard Spring Boot starter. | No (pre-approved in AD-5) |

---

## 14. Open Decisions

No blocking Open Decisions were identified. All Open Decisions (`OD-001` through `OD-004`) were resolved with Option A.

---

## 15. Planning Inputs

Key facts for `implementation-planner`:
1. Add `spring-boot-starter-validation` dependency to `pom.xml`.
2. Configure `application.yaml` for H2 file database, SQL init mode, and Hibernate validation.
3. Provide `src/main/resources/schema.sql` with explicit `CREATE TABLE IF NOT EXISTS customer`.
4. Create `Customer` entity with JPA auditing and explicit `@Column` definitions.
5. Create `CustomerRepository` extending `JpaRepository`.
6. Implement `PasswordValidator` / `@ValidPassword` and `RegisterCustomerRequest`.
7. Implement `CustomerService` with normalization (`trim().toLowerCase()`), duplicate check (`existsByEmail`), `BCryptPasswordEncoder`, and entity-to-DTO transformation.
8. Implement `CustomerController` with `@Valid` and `Location` header.
9. Implement `GlobalExceptionHandler` with AC-6 structured error format.
10. Implement comprehensive test suite (Unit, WebMvc, DataJpa, and SpringBootTest).

---

## 16. Traceability

| Acceptance Criterion | Specification Section | Design Artifact | Affected System Area | Test Category |
|---|---|---|---|---|
| **AC-001 (Successful Registration)** | §3 FR-001, FR-005, FR-007 | `api-design.md`, `db-design.md` | Controller, Service, Repository, Entity | Integration & WebMvc |
| **AC-002 (Unique Email)** | §3 FR-002, FR-003 | `api-design.md`, `db-design.md` | Service, Repository, Exception | Integration & Unit |
| **AC-003 (Email Validation)** | §5.1 | `api-design.md` | Request DTO, ExceptionHandler | WebMvc & Integration |
| **AC-004 (Password Storage)** | §3 FR-004, §5.2, §6 | `db-design.md`, `entity-model.md` | Security, Service, Entity | Integration & Unit |
| **AC-005 (Secure Response)** | §3 FR-007, §6 | `api-design.md`, `entity-model.md` | Response DTO, Controller | WebMvc & Integration |

---

## 17. Analysis Limitations

- Semantic code analysis via IntelliJ IDEA MCP was unavailable in this session; analysis was performed via built-in repository inspection (`TEXT_FALLBACK`). High confidence is maintained due to clean project baseline.

---

## 18. Readiness Result

**`PASS`** — The change surface is fully identified with high confidence, all risks are documented and mitigated, and the active Story is ready for `IMPLEMENTATION_PLANNING`.

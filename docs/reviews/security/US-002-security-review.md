---
artifact_type: security_review
story: US-002
version: 1
status: APPROVED
created_at: 2026-09-02T13:49:30Z
updated_at: 2026-09-02T13:49:30Z
produced_by: security-reviewer
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
  - path: docs/evidence/US-002-implementation-report.md
    version: 1
  - path: docs/verification/US-002-implementation-verification.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
critical_findings: 0
major_findings: 0
minor_findings: 0
informational_findings: 0
security_sensitive: true
runtime_checks: FULL
semantic_analysis: TEXT_FALLBACK
---

# Security Review — US-002: Customer Login

## 1. Executive Summary

- **Security Review Result:** `PASS`
- **Principal Security Controls:**
  - **BCrypt Password Verification:** Password matching strictly delegates to `BCryptPasswordEncoder.matches(rawPassword, storedHash)` (`SC-1`, `SEC-002`).
  - **Anti-Enumeration Protection:** Uniform `HTTP 401 Unauthorized` with generic message `"Invalid email or password"` returned for unknown emails, incorrect passwords, and disabled accounts (`SC-3`, `OD-002`).
  - **Zero Credential Exposure:** Plaintext passwords and BCrypt hashes are strictly excluded from response DTOs (`LoginResponse`), logs, and exception bodies (`SC-1`, `SC-9`, `AD-4`).
  - **Strict Access Control:** Default deny-all policy maintained across all endpoints with explicit public exception only for registration (`POST /api/v1/customers`) and login (`POST /api/v1/auth/login`) (`SC-4`).
  - **Persistence & Configuration Hardening:** `spring.h2.console.enabled=false` strictly enforced, `ddl-auto: validate` active, and generated database files git-ignored (`SC-6`, `SC-7`, `SC-8`).
- **Risks:** 0 Critical risks, 0 Major risks, 0 Minor risks.
- **Recommended Next Action:** Advance to `RECONCILIATION`.

---

## 2. Reviewed Artifacts

| Artifact Type | Path | Version | Status |
|---|---|---|---|
| `story` | `docs/stories/US-002-customer-login.md` | unversioned | Input |
| `specification` | `docs/specifications/US-002-spec.md` | 1 | APPROVED |
| `specification_review` | `docs/reviews/specifications/US-002-spec-review.md` | 1 | APPROVED |
| `api_design` | `docs/designs/api/US-002-api-design.md` | 1 | DRAFT |
| `openapi` | `docs/designs/api/US-002-openapi.yaml` | 1 | DRAFT |
| `database_design` | `docs/designs/database/US-002-db-design.md` | 1 | DRAFT |
| `entity_model` | `docs/designs/database/US-002-entity-model.md` | 1 | DRAFT |
| `design_review` | `docs/reviews/designs/US-002-design-review.md` | 1 | APPROVED |
| `impact_analysis` | `docs/impact-analysis/US-002-impact-analysis.md` | 1 | DRAFT |
| `implementation_plan` | `docs/plans/US-002-implementation-plan.md` | 1 | APPROVED |
| `plan_review` | `docs/reviews/plans/US-002-plan-review.md` | 1 | APPROVED |
| `test_strategy` | `docs/tests/US-002-test-strategy.md` | 1 | DRAFT |
| `ac_test_matrix` | `docs/tests/US-002-ac-test-matrix.md` | 1 | DRAFT |
| `implementation_report` | `docs/evidence/US-002-implementation-report.md` | 1 | DRAFT |
| `implementation_verification` | `docs/verification/US-002-implementation-verification.md` | 1 | APPROVED |
| `open_decisions` | `docs/decisions/US-002-open-decisions.md` | 2 | RESOLVED |

---

## 3. Security-Relevant Scope

- **Exposed Functionality:** Public HTTP endpoint `POST /api/v1/auth/login`.
- **Protected Assets:**
  - Customer credentials (plaintext passwords in transit, BCrypt password hashes in database).
  - Customer personal identity (`id`, `email`, `role`).
  - Session and authentication state (`SecurityContextHolder`, `JSESSIONID`).
- **Trust Boundaries:**
  - External Client → Controller (`AuthController`): Inbound untrusted JSON payload.
  - Controller → Service (`AuthService`): Validated DTO (`LoginRequest`).
  - Service → Persistence (`CustomerRepository`): Email-based query against indexed column.
  - Service → Security Context: Establishment of `UsernamePasswordAuthenticationToken` with granted authority `ROLE_CUSTOMER`.

---

## 4. Environment and Tools

- **Java Version:** OpenJDK 21
- **Spring Boot Version:** `4.0.0-M2` / Spring 7 stack
- **Active Profile:** Default / Test (in-memory H2)
- **Database Mode:** Local file-based H2; test in-memory H2
- **Review Tools:** Static code inspection, build/test execution evidence (`35/35` tests passing).

---

## 5. Authentication Review

- **Endpoint:** `POST /api/v1/auth/login` accepts `LoginRequest` (`email`, `password`).
- **Email Normalization:** `AuthService.login()` normalizes email input using `trim().toLowerCase()` before database lookup, ensuring consistency with registration (`OD-004`, `BR-002`).
- **Credential Matching:** Password matching uses `passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())` with `BCryptPasswordEncoder` (`SC-1`, `SEC-002`).
- **Account State:** Enforces `customer.isEnabled()` requirement; disabled accounts are blocked from authenticating (`BR-004`, `AC-004`).
- **Session Establishment:** Successful login sets a `UsernamePasswordAuthenticationToken` in `SecurityContextHolder` with granted authorities (`ROLE_CUSTOMER` / `ROLE_ADMIN`) (`SC-2`, `SC-3`).

---

## 6. Authorization Review

- **Access Policy:**
  - `POST /api/v1/customers`: Public (Registration).
  - `POST /api/v1/auth/login`: Public (Authentication).
  - `.anyRequest().authenticated()`: Default deny for all other endpoints (`SC-4`).
- **Role & Principal Mapping:** Roles mapped to Spring Security authorities prefixed with `ROLE_` (`ROLE_CUSTOMER`, `ROLE_ADMIN`).
- **Boundary Enforcement:** Service methods operate on DTOs and do not expose internal administrative capabilities.

---

## 7. Password and Credential Handling

- **In Transit:** Plaintext passwords are received strictly in the `LoginRequest` body and discarded after authentication.
- **Persistence:** Passwords are never stored in plain text; existing database entries contain BCrypt hashes (`VARCHAR(60)`).
- **Serialization:** `LoginResponse` DTO contains only `id`, `email`, `role`. No password or hash fields exist on the response model (`SC-1`, `AD-4`).
- **Logging & Diagnostics:** Passwords and hashes are absent from all logging statements and exception messages (`SC-9`).

---

## 8. Sensitive Data Exposure

- **API Responses:** `LoginResponse` contains exclusively non-sensitive identity metadata (`id`, `email`, `role`).
- **Test Evidence:** Integration test `CustomerLoginIntegrationTest.shouldNeverExposePasswordData` asserts that `password`, `passwordHash`, and `password_hash` JSON fields are not present in responses or headers.
- **Error Payloads:** `GlobalExceptionHandler` returns sanitized error responses without leaking stack traces, database metadata, SQL statements, or class names (`SC-9`).

---

## 9. Input Validation

- **Request DTO Constraints:**
  - `email`: `@NotBlank(message = "Email is required")`, `@Pattern(regexp = "^\\s*[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\s*$", message = "Invalid email format")`, `@Size(max = 255)`
  - `password`: `@NotBlank(message = "Password is required")`, `@Size(max = 72, message = "Password must not exceed 72 characters")` (enforcing BCrypt 72-byte limit from `security-conventions.md`)
- **Runtime Activation:** Handled by `@Valid` on `AuthController.login()` and intercepted by `GlobalExceptionHandler.handleValidationException()` returning structured `400 Bad Request`.
- **Media Type Restriction:** Requests missing or using invalid `Content-Type` return `415 Unsupported Media Type`.

---

## 10. API Security

- **Anti-Enumeration (`OD-002`):** Authentication failure scenarios (unregistered email, incorrect password, disabled account) uniformly return `HTTP 401 Unauthorized` with generic message `"Invalid email or password"`.
- **Response Headers:** `Set-Cookie: JSESSIONID=...; Path=/; HttpOnly` issued upon successful authentication.
- **Error Sanitization:** All error responses adhere to standard format (`timestamp`, `status`, `error`, `message`, `path`).

---

## 11. Persistence Security

- **Table Constraints:**
  - `password_hash VARCHAR(60) NOT NULL`
  - `email VARCHAR(255) NOT NULL UNIQUE` (`uq_customer_email`)
  - `enabled BOOLEAN NOT NULL DEFAULT TRUE`
- **Schema Management:** `ddl-auto: validate` prevents runtime DDL execution or schema tampering (`SC-8`).
- **Query Safety:** Spring Data JPA parameter binding prevents SQL injection risks.

---

## 12. H2 and Application Configuration

- **H2 Console:** Explicitly disabled (`spring.h2.console.enabled: false`) in `application.yaml` (`SC-6`).
- **Database Storage:** Local persistence uses file-based path `./data/customer-portal` (`PC-1`).
- **Git Hygiene:** Local database files (`./data/*.db`, `./data/*.mv.db`) and secret files (`.env*`, `.github_token`) are ignored in `.gitignore` (`SC-7`).

---

## 13. Logging and Telemetry

- **Application Logs:** No plaintext credentials, password hashes, or authorization headers logged.
- **Telemetry Hooks:** Observability hooks record operational metadata without logging request bodies containing credentials (`SC-9`).

---

## 14. Dependencies

- **POM Dependencies:** Unchanged from baseline. Standard Spring Boot Starter Security, Starter WebMVC, Starter Validation, Starter Data JPA, and H2 database runtime dependency.
- **Vulnerability Checks:** Standard dependencies managed via Spring Boot 4.x parent BOM.

---

## 15. Security Test Coverage

| Security Area | Test Suite | Test Method | Outcome |
|---|---|---|---|
| Valid Login & Context | `CustomerLoginIntegrationTest` | `shouldAuthenticateCustomerSuccessfully` | PASS |
| Email Normalization | `CustomerLoginIntegrationTest` | `shouldNormalizeEmailOnLogin` | PASS |
| Bad Password (Anti-Enumeration) | `CustomerLoginIntegrationTest` | `shouldRejectInvalidPassword` | PASS |
| Unknown Email (Anti-Enumeration) | `CustomerLoginIntegrationTest` | `shouldRejectNonExistentAccount` | PASS |
| Disabled Account | `CustomerLoginIntegrationTest` | `shouldRejectDisabledAccountLogin` | PASS |
| Credential Exclusion | `CustomerLoginIntegrationTest` | `shouldNeverExposePasswordData` | PASS |
| Unsupported Media Type | `CustomerLoginIntegrationTest` | `shouldRejectUnsupportedMediaType` | PASS |
| Blank/Malformed Email Validation | `CustomerLoginIntegrationTest` | `shouldRejectBlankEmail`, `shouldRejectInvalidEmailFormat` | PASS |
| Blank Password Validation | `CustomerLoginIntegrationTest` | `shouldRejectBlankPassword` | PASS |
| Unit Credential Checks | `AuthServiceTest` | `shouldThrowBadCredentialsOnPasswordMismatch`, `shouldThrowBadCredentialsOnMissingAccount`, `shouldThrowBadCredentialsOnDisabledAccount` | PASS |

---

## 16. Abuse Case Review

1. **Account Enumeration via Timing or Differential Error Messages:**
   - *Protection:* Uniform `401 Unauthorized` with identical message `"Invalid email or password"` returned across all authentication failures (`AC-002`, `AC-003`, `AC-004`).
   - *Status:* Verified.
2. **Credential Theft via Response Inspection:**
   - *Protection:* `LoginResponse` DTO strictly isolates output fields; password/hash fields do not exist.
   - *Status:* Verified.
3. **Disabled Account Access:**
   - *Protection:* `customer.isEnabled()` explicitly checked in service layer before establishing security context.
   - *Status:* Verified.
4. **Oversized Password DoS / Truncation Vulnerability:**
   - *Protection:* `@Size(max = 72)` enforces BCrypt input bounds at the validation layer.
   - *Status:* Verified.
5. **Unauthorized Endpoint Access:**
   - *Protection:* Spring Security default-deny configuration (`.anyRequest().authenticated()`).
   - *Status:* Verified.

---

## 17. Repository Hygiene

- No committed secrets, private keys, or credentials found in tracked repository files.
- `.gitignore` properly excludes `./data/*.db`, `.env`, `.env.*`, and `.github_token`.

---

## 18. Deviations

- None. Implementation conforms to all security conventions (`SC-1` through `SC-9`) and resolved Open Decisions (`OD-001` through `OD-004`).

---

## 19. Findings

| Finding ID | Severity | Category | Affected File / Artifact | Observed Evidence | Expected Security Behavior | Risk | Required Correction | Loop-Back Target |
|---|---|---|---|---|---|---|---|---|
| *None* | - | - | - | - | - | - | - | - |

- **Critical Findings:** 0
- **Major Findings:** 0
- **Minor Findings:** 0
- **Informational Findings:** 0

---

## 20. Positive Controls

1. Strong cryptographic hashing with `BCryptPasswordEncoder`.
2. Uniform anti-enumeration 401 error response across non-existent, bad password, and disabled accounts.
3. Strict DTO boundaries preventing persistence entity or credential leakage.
4. Server-side Jakarta Bean Validation enforcing email structure and password length limits.
5. Disabled H2 console and validated database DDL mode (`validate`).
6. Deny-by-default access control in Spring Security filter chain.

---

## 21. Open Decisions

No blocking security Open Decisions were identified. All Open Decisions (`OD-001`–`OD-004`) are resolved and respected.

---

## 22. Review Limitations

- Review conducted via source, configuration, and automated test execution analysis.
- External penetration testing, distributed denial of service (DDoS), and rate-limiting infrastructure are out of scope for the current MVP.

---

## 23. Verdict Rationale

**`PASS`** — The customer authentication implementation for `US-002` adheres to all project security conventions (`SC-1`–`SC-9`), business rules (`BR-002`, `BR-004`, `BR-005`), and stakeholder decisions (`OD-001`–`OD-004`). Passwords and hashes are strictly protected, anti-enumeration defenses are verified, and 0 Critical or Major findings exist. The story is approved to advance to `RECONCILIATION`.

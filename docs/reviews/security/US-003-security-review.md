---
artifact_type: security_review
story: US-003
version: 1
status: APPROVED
created_at: 2026-09-05T10:33:30Z
updated_at: 2026-09-05T10:33:30Z
produced_by: security-reviewer
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
  - path: docs/impact-analysis/US-003-impact-analysis.md
    version: 1
  - path: docs/plans/US-003-implementation-plan.md
    version: 1
  - path: docs/reviews/plans/US-003-plan-review.md
    version: 1
  - path: docs/tests/US-003-test-strategy.md
    version: 1
  - path: docs/tests/US-003-ac-test-matrix.md
    version: 1
  - path: docs/evidence/US-003-test-generation-report.md
    version: 1
  - path: docs/evidence/US-003-implementation-report.md
    version: 1
  - path: docs/verification/US-003-implementation-verification.md
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
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

# Security Review — US-003: View Customer Profile

## 1. Executive Summary

- **Security Review Result:** `PASS`
- **Principal Security Controls:**
  - **Service-Layer Resource Ownership Enforcement:** Inbound requests to `GET /api/v1/customers/{id}` strictly extract the authenticated user identity (`authentication.getName()`) and enforce customer isolation. If the requested `{id}` does not match the authenticated customer's ID, access is denied with `HTTP 403 Forbidden` (`message: "Access denied"`) prior to any target entity lookup (`SC-4`, `AC-002`, `OD-002`, `OD-004`).
  - **Mandatory Authentication & Default Deny:** Unauthenticated requests are rejected by Spring Security with `HTTP 401 Unauthorized` and an AC-6 compliant error envelope (`SC-4`, `SEC-001`, `AC-004`).
  - **Strict Credential & Sensitive Data Exclusion:** Response payload projection strictly uses `CustomerResponse` (`id`, `email`, `role`, `createdAt`). Plaintext passwords and `password_hash` are never serialized, logged, or exposed (`SC-1`, `SC-9`, `AC-003`).
  - **Standardized Error Hygiene:** All error responses adhere to `api-conventions.md` AC-6 JSON error format without revealing stack traces, database metadata, SQL statements, or class names (`SC-9`).
  - **Persistence & Configuration Hardening:** Read-only transaction semantics (`@Transactional(readOnly = true)`), `spring.h2.console.enabled=false`, `ddl-auto: validate`, and git exclusion of generated database files are preserved (`SC-6`, `SC-7`, `SC-8`).
- **Risks:** 0 Critical risks, 0 Major risks, 0 Minor risks.
- **Recommended Next Action:** Advance to `RECONCILIATION`.

---

## 2. Reviewed Artifacts

| Artifact Type | Path | Version | Status |
|---|---|---|---|
| `story` | `docs/stories/US-003-customer-profile-view.md` | unversioned | Input |
| `specification` | `docs/specifications/US-003-spec.md` | 1 | APPROVED |
| `specification_review` | `docs/reviews/specifications/US-003-spec-review.md` | 1 | APPROVED |
| `api_design` | `docs/designs/api/US-003-api-design.md` | 1 | DRAFT |
| `openapi` | `docs/designs/api/US-003-openapi.yaml` | 1 | DRAFT |
| `database_design` | `docs/designs/database/US-003-db-design.md` | 1 | DRAFT |
| `entity_model` | `docs/designs/database/US-003-entity-model.md` | 1 | DRAFT |
| `design_review` | `docs/reviews/designs/US-003-design-review.md` | 1 | APPROVED |
| `impact_analysis` | `docs/impact-analysis/US-003-impact-analysis.md` | 1 | DRAFT |
| `implementation_plan` | `docs/plans/US-003-implementation-plan.md` | 1 | APPROVED |
| `plan_review` | `docs/reviews/plans/US-003-plan-review.md` | 1 | APPROVED |
| `test_strategy` | `docs/tests/US-003-test-strategy.md` | 1 | DRAFT |
| `ac_test_matrix` | `docs/tests/US-003-ac-test-matrix.md` | 1 | DRAFT |
| `test_generation_report` | `docs/evidence/US-003-test-generation-report.md` | 1 | DRAFT |
| `implementation_report` | `docs/evidence/US-003-implementation-report.md` | 1 | DRAFT |
| `implementation_verification` | `docs/verification/US-003-implementation-verification.md` | 1 | APPROVED |
| `open_decisions` | `docs/decisions/US-003-open-decisions.md` | 2 | RESOLVED |

---

## 3. Security-Relevant Scope

- **Exposed Functionality:** HTTP endpoint `GET /api/v1/customers/{id}` requiring session authentication.
- **Protected Assets:**
  - Customer personal profile information (`id`, `email`, `role`, `createdAt`).
  - Credential secrecy (`password_hash` in persistence store).
  - Session authentication state (`SecurityContextHolder`, `JSESSIONID`).
- **Trust Boundaries:**
  - External Client → Controller (`CustomerController`): Inbound request validated for session authentication.
  - Controller → Service (`CustomerService`): Inbound path variable `id` and `Authentication` token.
  - Service Layer: Caller identity resolved from principal (`callerEmail`), caller's entity loaded from repository, and resource ownership verified (`callerCustomer.getId().equals(id)`). Cross-tenant access is terminated with `AccessDeniedException`.
  - Service → Output DTO: Projection into `CustomerResponse` without credential exposure.

---

## 4. Environment and Tools

- **Java Version:** OpenJDK 21
- **Spring Boot Version:** `4.1.1` / Spring 7 stack
- **Active Profile:** Default / Test (in-memory H2)
- **Database Mode:** Local file-based H2; test in-memory H2
- **Review Tools:** Static code inspection, build/test execution evidence (`48/48` tests passing, 0 failures, 0 errors, 0 skipped).

---

## 5. Authentication Review

- **Endpoint Protection:** `GET /api/v1/customers/{id}` is protected by Spring Security default rule `.anyRequest().authenticated()` (`SecurityConfig.java`) (`SC-4`, `SEC-001`).
- **Unauthenticated Handling:** Requests lacking valid authentication credentials or session cookie trigger `AuthenticationEntryPoint`, returning `HTTP 401 Unauthorized` with an AC-6 JSON error body (`message: "Full authentication is required to access this resource"`) (`AC-004`).
- **Principal Extraction:** `CustomerService.getCustomerProfile` securely extracts caller identity from `Authentication.getName()` (`SC-3`). If authentication or principal is null, access is denied immediately.

---

## 6. Authorization Review

- **Access Policy:**
  - Public endpoints: `POST /api/v1/customers` (Registration), `POST /api/v1/auth/login` (Login).
  - All other endpoints: Authenticated access required (`SC-4`).
- **Resource Ownership Verification:**
  - In accordance with `SC-4`, customer isolation is enforced at the Service layer (`CustomerService.java`).
  - The service loads the caller's account via `customerRepository.findByEmail(callerEmail)` and compares `callerCustomer.getId()` with the requested path variable `id`.
  - Mismatches throw `AccessDeniedException("Access denied")`, intercepted by `GlobalExceptionHandler` and rendered as `HTTP 403 Forbidden` (`OD-002`, `AC-002`).
  - Per `OD-004` Option A, the service does not query the requested `{id}` entity if it does not belong to the caller, completely mitigating resource probing and account enumeration.

---

## 7. Password and Credential Handling

- **Zero Plaintext Ingestion:** The profile viewing flow does not accept or process passwords.
- **Persistence Protection:** Existing stored hashes (`password_hash VARCHAR(60)`) are untouched and read-only during retrieval.
- **Serialization Isolation:** `CustomerResponse` DTO contains only `id`, `email`, `role`, and `createdAt`. No password or hash fields exist on the DTO (`SC-1`, `AC-003`).
- **Logging Safety:** No credentials or hashes are logged in `CustomerController` or `CustomerService` (`SC-9`).

---

## 8. Sensitive Data Exposure

- **API Responses:** `CustomerResponse` returns exclusively non-sensitive profile attributes (`id`, `email`, `role`, `createdAt`).
- **Test Evidence:** Integration test `CustomerProfileIntegrationTest.shouldNeverExposePasswordDataInProfileResponse` explicitly asserts that `password`, `passwordHash`, and `password_hash` JSON fields are absent from response bodies and headers.
- **Error Payloads:** `GlobalExceptionHandler` returns sanitized error payloads without leaking database schemas, SQL statements, stack traces, or entity internals (`SC-9`).

---

## 9. Input Validation

- **Path Variable Validation:**
  - Numeric identifier `Long id` is validated at the Spring MVC layer.
  - Non-numeric input (e.g., `/api/v1/customers/abc`) triggers `MethodArgumentTypeMismatchException`, intercepted by `GlobalExceptionHandler` and returned as `HTTP 400 Bad Request`.
  - Non-existent or mismatched IDs are rejected by ownership checks (`403 Forbidden`) or entity lookups (`404 Not Found`).

---

## 10. API Security

- **Approved Endpoints:** Only the approved endpoint `GET /api/v1/customers/{id}` is exposed, conforming to `docs/designs/api/US-003-openapi.yaml`.
- **HTTP Methods:** Only `GET` is supported for customer profile inspection; modifying operations remain out of scope.
- **Standard Error Format:** All error scenarios (`400`, `401`, `403`, `404`, `500`) return standardized JSON envelopes adhering to `api-conventions.md` AC-6.

---

## 11. Persistence Security

- **Read-Only Transaction:** `CustomerService.getCustomerProfile` is annotated with `@Transactional(readOnly = true)`, preventing accidental mutation.
- **Query Safety:** Primary key and email lookups utilize Spring Data JPA parameter binding, eliminating SQL injection vulnerabilities.
- **Schema Safety:** `ddl-auto: validate` prevents runtime modifications to table schema (`SC-8`).

---

## 12. H2 and Application Configuration

- **H2 Console:** Explicitly disabled (`spring.h2.console.enabled: false`) across configuration profiles (`SC-6`).
- **Database Storage:** Local runtime uses file-based path `./data/customer-portal` (`PC-1`).
- **Repository Hygiene:** Local database files (`./data/*.db`, `./data/*.mv.db`) and secret files (`.env*`, `.github_token`) are ignored in `.gitignore` (`SC-7`).

---

## 13. Logging and Telemetry

- **Application Logs:** No sensitive information, credentials, or authorization tokens are logged.
- **Telemetry Hooks:** Observability hooks record execution metadata only (timestamp, tool name, sizes), without logging sensitive request or response bodies (`SC-9`).

---

## 14. Dependencies

- **POM Dependencies:** Unchanged from baseline. Standard Spring Boot Starters (`security`, `webmvc`, `validation`, `data-jpa`) and H2 runtime.
- **Vulnerability Checks:** Managed via Spring Boot 4.1.1 parent BOM.

---

## 15. Security Test Coverage

| Security Area | Test Suite | Test Method | Outcome |
|---|---|---|---|
| View Own Profile (200 OK) | `CustomerProfileIntegrationTest` | `shouldReturnProfileWhenCustomerViewsOwnData` | PASS |
| Cross-Tenant Access (403 Forbidden) | `CustomerProfileIntegrationTest` | `shouldReturn403ForbiddenWhenAccessingAnotherCustomerProfile` | PASS |
| Credential Exclusion (AC-003) | `CustomerProfileIntegrationTest` | `shouldNeverExposePasswordDataInProfileResponse` | PASS |
| Unauthenticated Access (401) | `CustomerProfileIntegrationTest` | `shouldReturn401UnauthorizedWhenUnauthenticated` | PASS |
| Non-Numeric Path Variable (400) | `CustomerProfileIntegrationTest` | `shouldReturn400BadRequestWhenIdIsNotNumeric` | PASS |
| Missing Customer Entity (404) | `CustomerProfileIntegrationTest` | `shouldReturn404NotFoundWhenOwnProfileDoesNotExist` | PASS |
| Unit: Ownership Check | `CustomerServiceTest` | `shouldReturnProfileWhenCallerOwnsResource` | PASS |
| Unit: Ownership Mismatch | `CustomerServiceTest` | `shouldThrowAccessDeniedWhenCallerDoesNotOwnResource` | PASS |
| Unit: Missing Caller Account | `CustomerServiceTest` | `shouldThrowCustomerNotFoundWhenCallerNotInRepository` | PASS |
| Unit: Null Authentication | `CustomerServiceTest` | `shouldThrowAccessDeniedWhenAuthenticationIsNull` | PASS |
| Controller: Slice Profile 200 OK | `CustomerControllerTest` | `shouldReturnCustomerProfileWhenFound` | PASS |
| Controller: Slice Invalid Variable | `CustomerControllerTest` | `shouldReturn400WhenPathVariableIsInvalid` | PASS |

---

## 16. Abuse Case Review

1. **Horizontal Privilege Escalation (IDOR / Cross-Tenant Access):**
   - *Protection:* Service layer extracts authenticated email from `SecurityContext` and verifies that caller ID equals requested path parameter `id`. Requests for other customers' IDs return `403 Forbidden` (`AC-002`, `SC-4`).
   - *Status:* Verified.
2. **Account Enumeration / ID Probing:**
   - *Protection:* Per `OD-004` Option A, the service validates ownership before checking resource existence. Callers attempting to query other IDs receive `403 Forbidden` without querying the target ID, preventing existence discovery.
   - *Status:* Verified.
3. **Unauthenticated Access:**
   - *Protection:* Default deny in Spring Security filter chain returns `401 Unauthorized` with standard error body.
   - *Status:* Verified.
4. **Credential Leakage in API Response:**
   - *Protection:* Projection DTO `CustomerResponse` does not declare password or hash fields. Automated test verifies field absence.
   - *Status:* Verified.
5. **Path Parameter Injection / Type Corruption:**
   - *Protection:* Type conversion error caught by `GlobalExceptionHandler` returning `400 Bad Request`.
   - *Status:* Verified.

---

## 17. Repository Hygiene

- No committed secrets, tokens, private keys, or environment files in the repository.
- `.gitignore` properly excludes database files, local IDE configs, and sensitive files.

---

## 18. Deviations

- None. Implementation conforms to all security conventions (`SC-1` through `SC-9`), business rules, and resolved Open Decisions (`OD-001` through `OD-004`).

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

1. Service-layer ownership verification preventing horizontal privilege escalation (`SC-4`).
2. Timing attack and ID enumeration mitigation by verifying caller ownership prior to entity queries (`OD-004`).
3. Clean DTO boundary (`CustomerResponse`) ensuring complete isolation of password hashes (`SC-1`, `AC-003`).
4. Default deny authorization policy in Spring Security filter chain (`SC-4`).
5. Standardized error envelope adhering to `api-conventions.md` AC-6 schema (`SC-9`).
6. Enforced `spring.h2.console.enabled=false` and `ddl-auto: validate` (`SC-6`, `SC-8`).

---

## 21. Open Decisions

No blocking security Open Decisions were identified. All Open Decisions (`OD-001` through `OD-004`) are resolved and respected.

---

## 22. Review Limitations

- Review conducted via static code inspection, configuration auditing, and automated test execution.
- Advanced infrastructure security (e.g. WAF, network rate-limiting, DDoS mitigation) is outside MVP scope.

---

## 23. Verdict Rationale

**`PASS`** — The customer profile viewing implementation for `US-003` adheres to all project security conventions (`SC-1` through `SC-9`), business rules, and stakeholder decisions (`OD-001` through `OD-004`). In particular, horizontal privilege escalation is prevented via service-layer ownership validation, credentials and hashes are excluded from API responses, unauthenticated access is rejected with `401 Unauthorized`, and error payloads adhere strictly to AC-6 standards. With 0 Critical and 0 Major findings and 100% passing tests, the story is approved to advance to `RECONCILIATION`.

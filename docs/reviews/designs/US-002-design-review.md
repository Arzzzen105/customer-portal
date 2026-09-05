---
artifact_type: design_review
story: US-002
version: 1
status: APPROVED
created_at: 2026-09-02T13:28:44Z
updated_at: 2026-09-02T13:28:44Z
produced_by: design-reviewer
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/reviews/specifications/US-002-spec-review.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
  - path: docs/designs/api/US-002-api-design.md
    version: 1
  - path: docs/designs/api/US-002-openapi.yaml
    version: 1
  - path: docs/designs/database/US-002-db-design.md
    version: 1
  - path: docs/designs/database/US-002-entity-model.md
    version: 1
supersedes: null
---

# Design Review — US-002: Customer Login

## 1. Summary

This review independently evaluates the API design ([docs/designs/api/US-002-openapi.yaml](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-002-openapi.yaml), [docs/designs/api/US-002-api-design.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-002-api-design.md)) and Database design ([docs/designs/database/US-002-db-design.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/database/US-002-db-design.md), [docs/designs/database/US-002-entity-model.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/database/US-002-entity-model.md)) for **US-002: Customer Login** against the approved Specification ([docs/specifications/US-002-spec.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/specifications/US-002-spec.md)), architecture conventions, security conventions, and cross-model consistency.

---

## 2. Reviewed Artifacts

| Artifact Type | Path | Version | Status |
|---|---|---|---|
| `story` | `docs/stories/US-002-customer-login.md` | unversioned | Input |
| `specification` | `docs/specifications/US-002-spec.md` | 1 | APPROVED |
| `specification_review` | `docs/reviews/specifications/US-002-spec-review.md` | 1 | APPROVED |
| `open_decisions` | `docs/decisions/US-002-open-decisions.md` | 2 | RESOLVED |
| `api_design` | `docs/designs/api/US-002-api-design.md` | 1 | DRAFT |
| `openapi` | `docs/designs/api/US-002-openapi.yaml` | 1 | DRAFT |
| `database_design` | `docs/designs/database/US-002-db-design.md` | 1 | DRAFT |
| `entity_model` | `docs/designs/database/US-002-entity-model.md` | 1 | DRAFT |

---

## 3. API Design Review

- **Endpoint Structure & Routing:** `POST /api/v1/auth/login` conforms to REST conventions (`api-conventions.md` AC-1, AC-3) and stakeholder decision `OD-001` Option A.
- **Acceptance Criteria Coverage:**
  - `AC-001` (Successful Login): Operation `POST /api/v1/auth/login` returns `200 OK` with `Set-Cookie` header (`JSESSIONID`) and body `LoginResponse` containing `id`, `email`, and `role`.
  - `AC-002` (Invalid Password), `AC-003` (Unknown Account), `AC-004` (Disabled Account): Handled uniformly with `401 Unauthorized` and standard error payload (`message: "Invalid email or password"`), preventing account enumeration (`OD-002`, `SC-3`).
  - `AC-005` (Secure Authentication Response): Response schemas omit plaintext passwords and BCrypt hashes (`SC-1`, `SC-9`, `AD-4`).
- **DTOs & Layer Isolation:** Request binds to `LoginRequest` (in `model.request`) and response to `LoginResponse` (in `model.dto`). No entity classes leak across the API boundary (`AD-4`).
- **Validation Constraints:** Jakarta Bean Validation constraints on `LoginRequest` match Specification requirements:
  - `email`: `@NotBlank`, `@Email`, `@Size(max = 255)`
  - `password`: `@NotBlank`, `@Size(max = 72)`
- **Error Model Compliance:** Error responses (`400 Bad Request`, `401 Unauthorized`, `415 Unsupported Media Type`, `500 Internal Server Error`) strictly adhere to `api-conventions.md` AC-6 JSON error shape.
- **Backward Compatibility:** Introduces new authentication endpoint without modifying existing customer endpoints (`POST /api/v1/customers`).

---

## 4. Database Design Review

- **Domain Model Tracing:** Reuses the existing `customer` entity (`model.entity.Customer`) established in `US-001`, fully aligned with `business-glossary.md` concepts.
- **Explicit Schema & Constraints:**
  - Columns: `id` (BIGINT PK), `email` (VARCHAR(255) NOT NULL UNIQUE), `password_hash` (VARCHAR(60) NOT NULL), `role` (VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER'), `enabled` (BOOLEAN NOT NULL DEFAULT TRUE), `created_at` / `updated_at` (TIMESTAMP WITH TIME ZONE NOT NULL).
  - Explicit column lengths and constraints avoid reliance on JPA defaults (`PC-4`).
  - Primary key `pk_customer` and natural key index `uq_customer_email` explicitly named (`PC-5`, `PC-7`).
- **Schema Initialization:** Verified compatibility with `src/main/resources/schema.sql` under Hibernate `ddl-auto: validate` (`PC-2`, `SC-8`). No schema migration required for US-002.
- **Sensitive Data Handling:** `password_hash` (VARCHAR(60)) stores exclusively BCrypt hash outputs. Plaintext passwords are never persisted (`PC-9`, `SC-1`, `NFR-001`).
- **Auditing & Timestamps:** UTC timestamps managed via JPA Auditing (`AuditingEntityListener`, `PC-6`, `BR-007`).
- **Repository Interface:** `CustomerRepository.findByEmail(String email)` queries against indexed unique column `uq_customer_email` (`PC-7`).

---

## 5. Cross-Model Consistency

- **Contract to Persistence Alignment:**
  - `LoginRequest.email` (`String`, max 255) maps directly to `Customer.email` (`VARCHAR(255)`).
  - `LoginRequest.password` is verified against `Customer.password_hash` (`VARCHAR(60)` BCrypt hash).
  - `Customer.id`, `Customer.email`, `Customer.role` map directly to `LoginResponse.id`, `LoginResponse.email`, `LoginResponse.role`.
  - `Customer.enabled` maps directly to authentication eligibility checks (`BR-004`, `AC-004`).
- **Business Rule Enforcement:**
  - Email uniqueness and case-insensitive comparison (`BR-001`, `BR-002`) are supported by service-layer normalization (`trim().toLowerCase()` per `OD-004`) and unique index `uq_customer_email`.
  - Disabled account restriction (`BR-004`) is checked at service layer and unified under 401 response per `OD-002`.

---

## 6. Security Review of Designs

- **Public Access Configuration:** `POST /api/v1/auth/login` is explicitly declared as a public endpoint (`SC-4`, `security: []`).
- **Anti-Enumeration Defense:** Generic `401 Unauthorized` (`"Invalid email or password"`) for missing accounts, bad passwords, and disabled accounts prevents user enumeration attacks (`SC-3`, `OD-002`).
- **Credential Protection:** Plaintext passwords exist only transiently in `LoginRequest` and are never serialized into response DTOs, audit logs, or error responses (`SC-1`, `SC-9`, `AD-4`).
- **Session Management:** Successful login creates a session context and issues an `HttpOnly` `JSESSIONID` cookie (`SC-3`, `AC-7`).

---

## 7. Findings

| Finding ID | Severity | Area | Description | Required Correction | Status |
|---|---|---|---|---|---|
| *None* | - | - | Zero Critical, Major, or Minor findings identified. | - | PASS |

- **Critical Findings:** 0
- **Major Findings:** 0
- **Minor Findings:** 0

---

## 8. Open Decisions

All Open Decisions in [docs/decisions/US-002-open-decisions.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/decisions/US-002-open-decisions.md) are `RESOLVED`:
- `OD-001` (Endpoint routing): Resolved to `POST /api/v1/auth/login`.
- `OD-002` (Anti-enumeration error handling): Resolved to uniform `401 Unauthorized` (`"Invalid email or password"`).
- `OD-003` (Login response shape): Resolved to `{ id, email, role }`.
- `OD-004` (Email normalization): Resolved to service-layer trim + lowercase.

No unresolved open decisions remain.

---

## 9. Limitations

- MFA, OAuth2/Social Login, and Password Recovery are explicitly out of scope per User Story and Specification.
- Session concurrency controls and remember-me tokens are not included in the MVP scope.

---

## 10. Verdict

**`PASS`** — Both API and Database designs are complete, consistent, secure, compliant with all architectural standards, and ready for `IMPACT_ANALYSIS`.

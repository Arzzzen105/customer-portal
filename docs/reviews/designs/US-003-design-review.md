---
artifact_type: design_review
story: US-003
version: 1
status: APPROVED
created_at: 2026-09-05T10:09:30Z
updated_at: 2026-09-05T10:09:30Z
produced_by: design-reviewer
inputs:
  - path: docs/stories/US-003-customer-profile-view.md
    version: null
  - path: docs/specifications/US-003-spec.md
    version: 1
  - path: docs/reviews/specifications/US-003-spec-review.md
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
    version: 2
  - path: docs/designs/api/US-003-api-design.md
    version: 1
  - path: docs/designs/api/US-003-openapi.yaml
    version: 1
  - path: docs/designs/database/US-003-db-design.md
    version: 1
  - path: docs/designs/database/US-003-entity-model.md
    version: 1
supersedes: null
---

# Design Review — US-003: View Customer Profile

## 1. Summary

This review independently evaluates the API design ([docs/designs/api/US-003-openapi.yaml](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-003-openapi.yaml), [docs/designs/api/US-003-api-design.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-003-api-design.md)) and Database design ([docs/designs/database/US-003-db-design.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/database/US-003-db-design.md), [docs/designs/database/US-003-entity-model.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/database/US-003-entity-model.md)) for **US-003: View Customer Profile** against the approved Specification ([docs/specifications/US-003-spec.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/specifications/US-003-spec.md)), architecture conventions, security conventions, and cross-model consistency.

---

## 2. Reviewed Artifacts

| Artifact Type | Path | Version | Status |
|---|---|---|---|
| `story` | `docs/stories/US-003-customer-profile-view.md` | unversioned | Input |
| `specification` | `docs/specifications/US-003-spec.md` | 1 | APPROVED |
| `specification_review` | `docs/reviews/specifications/US-003-spec-review.md` | 1 | APPROVED |
| `open_decisions` | `docs/decisions/US-003-open-decisions.md` | 2 | APPROVED |
| `api_design` | `docs/designs/api/US-003-api-design.md` | 1 | DRAFT |
| `openapi` | `docs/designs/api/US-003-openapi.yaml` | 1 | DRAFT |
| `database_design` | `docs/designs/database/US-003-db-design.md` | 1 | DRAFT |
| `entity_model` | `docs/designs/database/US-003-entity-model.md` | 1 | DRAFT |

---

## 3. API Design Review

- **Endpoint Structure & REST Conventions:**
  - Route: `GET /api/v1/customers/{id}` correctly uses plural noun resource naming, path versioning, and HTTP `GET` for single-resource retrieval (`api-conventions.md` AC-1, AC-3, AC-4, `OD-001` Option A).
  - Media Type: Strictly `application/json` (`api-conventions.md` AC-2).
- **Acceptance Criteria Coverage:**
  - `AC-001` (View Own Profile): `GET /customers/{id}` returns `200 OK` with JSON response body matching `CustomerResponse` containing `{ id, email, role, createdAt }`.
  - `AC-002` (Ownership Enforcement): Unauthorized cross-account access attempts return `403 Forbidden` with standard error envelope (`message: "Access denied"`), fulfilling `OD-002` Option A and `SEC-003`.
  - `AC-003` (Sensitive Data Exclusion): Neither plaintext passwords nor `passwordHash` (nor internal flags like `enabled` or `updatedAt`) are exposed in `CustomerResponse` (`SC-1`, `SC-9`, `AD-4`).
  - `AC-004` (Consistent Response): Standard HTTP status codes (`200`, `400`, `401`, `403`, `404`, `500`) and the standard error envelope (`api-conventions.md` AC-6) are adhered to across all responses.
- **DTOs & Layer Isolation:**
  - Response utilizes DTO `CustomerResponse` in `org.example.customerportal.model.dto`. No JPA entities are returned directly by the controller, maintaining strict layer boundaries (`AD-4`, `package-map.md`).
- **Validation Rules & Error Model:**
  - Path parameter `id` is specified as `integer` (int64) with `minimum: 1`. Type mismatch errors (e.g. non-numeric input) return `400 Bad Request`.
  - Unauthenticated calls return `401 Unauthorized` (`SC-4`).
  - Resource existence check returns `404 Not Found` when the caller queries their own non-existent ID (`OD-004`).
  - Error responses strictly follow the five-field schema `{ timestamp, status, error, message, path }` (`AC-6`, `SC-9`).
- **Backward Compatibility:**
  - Adds a new `GET /api/v1/customers/{id}` operation to the existing `CustomerController` without altering existing endpoints (`POST /api/v1/customers` or `POST /api/v1/auth/login`).

---

## 4. Database Design Review

- **Domain Model Tracing:**
  - Reuses the existing `customer` aggregate (`org.example.customerportal.model.entity.Customer`) defined in US-001, tracing directly to concepts in `docs/product/business-glossary.md`.
- **Explicit Schema & Constraints:**
  - Table: `customer` with columns `id` (`BIGINT AUTO_INCREMENT PK`), `email` (`VARCHAR(255) NOT NULL UNIQUE`), `password_hash` (`VARCHAR(60) NOT NULL`), `role` (`VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER'`), `enabled` (`BOOLEAN NOT NULL DEFAULT TRUE`), `created_at` (`TIMESTAMP WITH TIME ZONE NOT NULL`), and `updated_at` (`TIMESTAMP WITH TIME ZONE NOT NULL`).
  - All columns declare explicit lengths, nullability, and constraints without reliance on JPA defaults (`PC-4`).
  - Constraint names `pk_customer` and `uq_customer_email` conform to project naming rules (`PC-5`).
- **Schema Initialization & Migrations:**
  - Verified compatible with `src/main/resources/schema.sql` under Hibernate `ddl-auto: validate` (`PC-2`, `SC-8`). No schema alterations or DDL scripts are needed for US-003.
- **Performance & Indexing:**
  - Point lookup `findById(id)` executes via the clustered primary key index `pk_customer`, ensuring optimal $O(1)$ query complexity (`PC-7`, `NFR-004`).
- **Transaction Semantics:**
  - Service-level operation is read-only (`@Transactional(readOnly = true)`), preventing dirty reads and unwanted locks.

---

## 5. Cross-Model Consistency

- **API Schema to Entity Alignment:**
  - `Customer.id` (`Long` / `BIGINT`) $\leftrightarrow$ `CustomerResponse.id` (`integer`, `int64`).
  - `Customer.email` (`String` / `VARCHAR(255)`) $\leftrightarrow$ `CustomerResponse.email` (`string`, `email`).
  - `Customer.role` (`String` / `VARCHAR(20)`) $\leftrightarrow$ `CustomerResponse.role` (`string`).
  - `Customer.createdAt` (`Instant` / `TIMESTAMP WITH TIME ZONE`) $\leftrightarrow$ `CustomerResponse.createdAt` (`string`, `date-time`).
- **Credential & Internal State Isolation:**
  - `Customer.passwordHash` is excluded from the DTO.
  - `Customer.enabled` and `Customer.updatedAt` are internal state and excluded from `CustomerResponse` per `OD-003` Option A.
- **Execution Precedence Alignment:**
  - Service-layer ownership verification precedes database queries per `OD-004` Option A, ensuring that caller identity is validated against the path variable before executing any SQL select.

---

## 6. Security Review of Designs

- **Access Control & Tenant Isolation:**
  - Endpoint requires authentication (`anyRequest().authenticated()`, `cookieAuth`) (`SC-4`, `SEC-001`).
  - Service-layer ownership verification (`SEC-002`, `SC-4`) strictly compares the authenticated principal's customer ID against requested `{id}`. Cross-customer requests are denied with `403 Forbidden` (`OD-002` Option A).
- **Anti-Enumeration & Query Precedence:**
  - By validating ownership before executing repository queries (`OD-004`), unauthorized users cannot probe customer ID existence, mitigating ID enumeration and timing side-channels.
- **Credential Secrecy & Data Hygiene:**
  - Password hashes are completely shielded from DTO serialization (`SC-1`, `SC-9`, `AC-003`).
  - Error responses follow `api-conventions.md` AC-6, omitting stack traces, SQL, class names, or internal paths (`SC-9`).

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

All Open Decisions in [docs/decisions/US-003-open-decisions.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/decisions/US-003-open-decisions.md) are `APPROVED` / `RESOLVED`:
- **OD-001:** Profile Endpoint URI and Routing $\rightarrow$ `GET /api/v1/customers/{id}` (Option A).
- **OD-002:** HTTP Status Code for Unauthorized Access $\rightarrow$ `403 Forbidden` with standard error payload (Option A).
- **OD-003:** Profile Response Payload Schema $\rightarrow$ Reuse `CustomerResponse` `{ id, email, role, createdAt }` (Option A).
- **OD-004:** Ownership Check Precedence vs Resource Existence $\rightarrow$ Check ownership first, reject with 403 before entity lookup (Option A).

No unresolved Open Decisions remain.

---

## 9. Limitations

- Profile editing/update (`PUT`/`PATCH /api/v1/customers/{id}`) is out of scope.
- Account administration, deletion, and customer listing (`GET /api/v1/customers`) are out of scope.

---

## 10. Verdict

**`PASS`** — Both API and Database designs are complete, consistent, secure, compliant with all architectural and persistence conventions, and fully traceable to the approved Specification. The story is ready to proceed to `IMPACT_ANALYSIS`.

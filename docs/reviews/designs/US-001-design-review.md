---
artifact_type: design_review
story: US-001
version: 1
status: APPROVED
created_at: 2026-08-31T15:59:30Z
updated_at: 2026-08-31T15:59:30Z
produced_by: design-reviewer
inputs:
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
  - path: docs/decisions/US-001-open-decisions.md
    version: 2
supersedes: null
---

# Design Review — US-001: Customer Registration

## 1. Summary

This review independently evaluates the API design ([docs/designs/api/US-001-openapi.yaml](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-001-openapi.yaml), [docs/designs/api/US-001-api-design.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-001-api-design.md)) and Database design ([docs/designs/database/US-001-db-design.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/database/US-001-db-design.md), [docs/designs/database/US-001-entity-model.md](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/database/US-001-entity-model.md)) for **US-001: Customer Registration** against the approved Specification, architectural conventions, security conventions, and cross-model consistency.

---

## 2. Reviewed Artifacts

| Artifact Type | Path | Version |
|---|---|---|
| `specification` | `docs/specifications/US-001-spec.md` | 1 (APPROVED) |
| `api_design` | `docs/designs/api/US-001-api-design.md` | 1 |
| `openapi` | `docs/designs/api/US-001-openapi.yaml` | 1 |
| `database_design` | `docs/designs/database/US-001-db-design.md` | 1 |
| `entity_model` | `docs/designs/database/US-001-entity-model.md` | 1 |
| `open_decisions` | `docs/decisions/US-001-open-decisions.md` | 2 (RESOLVED) |

---

## 3. Evaluation Dimensions

### 3.1 API Design Review
- **Endpoint Structure:** `POST /api/v1/customers` conforms to REST conventions (plural nouns, kebab-case segments, path versioning per `api-conventions.md` AC-1, AC-3).
- **Request / Response Schemas:** Strict usage of dedicated DTOs (`RegisterCustomerRequest`, `CustomerResponse`). No persistent entities exposed in controller boundaries (`AD-4`).
- **Validation Constraints:** Exact mirroring of specification rules on `email` (`@NotBlank`, `@Email`, max 255) and `password` (`@NotBlank`, length 12–72, uppercase/lowercase/digit/special regex).
- **Status Codes & Headers:** `201 Created` with `Location: /api/v1/customers/{id}` header on success; `400 Bad Request` with field errors; `409 Conflict` on duplicate email; `415 Unsupported Media Type` on wrong header; `500 Internal Server Error` fallback. Error formats strictly adhere to AC-6.
- **Security:** Public endpoint with anonymous access configured per `security-conventions.md` SC-4.

### 3.2 Database Design Review
- **Schema & Constraints:** Table `customer` has explicit non-null column lengths (`email` VARCHAR(255), `password_hash` VARCHAR(60), `role` VARCHAR(20), `enabled` BOOLEAN, `created_at` / `updated_at` TIMESTAMP WITH TIME ZONE).
- **Primary & Natural Keys:** Surrogate `id` (BIGINT IDENTITY PK, `PC-3`), unique natural key constraint `uq_customer_email` on `email` (`PC-4`, `PC-5`).
- **Schema Initialization:** DDL script provided for `src/main/resources/schema.sql` (`PC-2`) compatible with `ddl-auto=validate`.
- **Sensitive Data Storage:** Plaintext passwords are not persisted; only the 60-character BCrypt hash is stored in `password_hash` (`PC-9`, `SC-1`).
- **Auditing:** JPA auditing for UTC timestamps `created_at` and `updated_at` (`PC-6`, `BR-007`).

### 3.3 Cross-Model Consistency
- **Field Matching:**
  - `email`: `String` / `VARCHAR(255)` in both API request DTO, entity, and DB schema.
  - `role`: `String` / `VARCHAR(20)` (`"CUSTOMER"`) consistent across entity and response DTO.
  - `createdAt`: `Instant` / `TIMESTAMP WITH TIME ZONE` mapped from entity to response DTO.
- **Layer Separation:** Full DTO/Entity boundary respected (`AD-4`). Service layer owns password hashing and entity↔DTO mapping.
- **Business Rule Enforcement:** Uniqueness and case-insensitivity (`BR-001`, `BR-002`) are implemented via service-level normalization (trim + lowercase) and enforced by the database unique constraint `uq_customer_email`.

### 3.4 Security Review of Designs
- Sensitive credential protection: Plaintext password and password hash are omitted from response schemas, error responses, and audit logs (`AC-005`, `SC-1`, `SC-9`, `AD-4`).
- Error responses suppress internal exception details (`SC-9`, `AC-6`).

---

## 4. Findings

| Finding ID | Severity | Area | Description | Status |
|---|---|---|---|---|
| *None* | - | - | Zero Critical, Major, or Minor findings identified. | PASS |

- **Critical Findings:** 0
- **Major Findings:** 0
- **Minor Findings:** 0

---

## 5. Verdict

**`PASS`** — Both API and Database designs are complete, consistent with each other, aligned with all architectural and security conventions, and ready for `IMPACT_ANALYSIS`.

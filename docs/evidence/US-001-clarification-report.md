---
artifact_type: clarification_report
story: US-001
version: 1
status: DRAFT
created_at: 2026-08-31T15:41:40Z
updated_at: 2026-08-31T15:41:40Z
produced_by: us-clarifier
inputs:
  - path: docs/stories/US-001-register-customer.md
    version: null
supersedes: null
---

# Clarification Report — US-001: Customer Registration

## 1. Executive Summary

This report documents the clarification analysis for **US-001: Customer Registration**, preparing the story for specification authoring (`spec-writer`). The scope, actors, business rules, acceptance criteria, security requirements, and open decisions have been analyzed against product documentation and architectural conventions.

---

## 2. Business Intent & Context

- **Story ID:** `US-001`
- **Title:** Customer Registration
- **Epic:** `EPIC-1`
- **Primary Actor:** Customer (external user self-registering on the platform)
- **Business Value:** Enables customers to self-register without administrative intervention, establishing their digital identity on the Customer Portal.
- **Core Action:** A prospective customer submits an email address and plaintext password to establish a new account.

---

## 3. Acceptance Criteria Analysis

| Criteria ID | Summary | Clarification & Invariants |
|---|---|---|
| **AC-001** | Successful Registration | Valid email and password result in account creation, `ROLE_CUSTOMER` assignment, and enabled status for subsequent authentication. |
| **AC-002** | Unique Email | Existing email registration attempts are rejected; duplicates are prevented per `BR-001` and `BR-002`. |
| **AC-003** | Email Validation | Invalid email format rejected with descriptive validation error response (`400 Bad Request`). |
| **AC-004** | Password Storage | Passwords are never persisted in plaintext (`BR-005`, `SC-1`, `NFR-001`); stored exclusively as a BCrypt hash in `password_hash` (`VARCHAR(60)`). |
| **AC-005** | Secure Response | Registration response payloads must never contain plaintext passwords, password hashes, or sensitive internal credentials (`SC-1`, `AD-4`). |

---

## 4. Architectural & Security Alignments

1. **Security Policy Alignment (`security-conventions.md`):**
   - Password strength requirements: minimum length 12, maximum length 72, at least one uppercase, one lowercase, one digit, and one special character.
   - Hashing: `BCryptPasswordEncoder` bean in the `security` package.
   - Default role: `CUSTOMER` (authority string `ROLE_CUSTOMER`).
   - Default account status: `enabled = true`.
   - Public endpoint: Registration endpoint is publicly accessible without prior authentication (`SC-4`).

2. **API Conventions Alignment (`api-conventions.md`):**
   - Path versioning: `/api/v1/...`.
   - Content-Type: `application/json` (UTF-8).
   - Expected status codes: `201 Created` on success, `400 Bad Request` on Bean Validation failure, `409 Conflict` on duplicate email conflict.
   - Error body format: Strictly adhering to AC-6 structure (`timestamp`, `status`, `error`, `message`, `path`).

3. **Persistence Conventions Alignment (`persistence-conventions.md`):**
   - Table: `customer` (singular, snake_case).
   - Primary Key: `id` (`BIGINT` / `Long`, Identity auto-increment).
   - Unique Constraint: `uq_customer_email` on `email`.
   - Audit Columns: `created_at` and `updated_at` (UTC timestamps via JPA Auditing).

---

## 5. Ambiguities, Contradictions, and Missing Requirements

1. **Email Casing and Collation:** `BR-002` specifies case-insensitive comparison, but does not state whether email strings are normalized (lowercased) at ingestion or compared using SQL lowercase functions. Captured as **OD-001**.
2. **Registration Endpoint Routing:** REST conventions (`api-conventions.md`) recommend `POST /api/v1/customers`, whereas some auth systems use `/api/v1/auth/register`. Captured as **OD-002**.
3. **Response Representation:** `AC-005` specifies that passwords/hashes must not be returned, but the exact response fields (`id`, `email`, `role`, `createdAt`) need formal contract definition. Captured as **OD-003**.
4. **Duplicate Handling Semantics:** HTTP status `409 Conflict` vs `400 Bad Request` vs silent handling. Captured as **OD-004**.

---

## 6. Out of Scope Verification

In alignment with `docs/stories/US-001-register-customer.md`:
- Customer Login / Session Creation (`OUT_OF_SCOPE`)
- Password Reset / Recovery (`OUT_OF_SCOPE`)
- Email Verification via Token / Mail Sender (`OUT_OF_SCOPE`)
- Multi-Factor Authentication (MFA) (`OUT_OF_SCOPE`)
- Multi-step account activation workflows (`OUT_OF_SCOPE`)

---

## 7. Open Decisions Reference

All identified open decisions are formally tracked in `docs/decisions/US-001-open-decisions.md`:

- **OD-001:** Email Normalization and Case-Insensitive Storage Strategy (Status: `OPEN`)
- **OD-002:** Customer Registration Endpoint Path (Status: `OPEN`)
- **OD-003:** Registration Success Response Payload and Location Header (Status: `OPEN`)
- **OD-004:** Duplicate Email Conflict Handling and Error Payload (Status: `OPEN`)

---

## 8. Specification Authoring Checklist (`spec-writer`)

The upcoming Specification (`docs/specifications/US-001-spec.md`) must cover:
- [ ] Detailed functional specification mapping all 5 Acceptance Criteria (`AC-001` through `AC-005`).
- [ ] Concrete endpoint definition (`POST /api/v1/customers`), request payload DTO schema, and response DTO schema.
- [ ] Explicit Bean Validation rules for `email` (format, non-empty, max length) and `password` (min 12, max 72, complexity).
- [ ] Service layer orchestration: duplicate check (`BR-001`, `BR-002`), password hashing with BCrypt (`SC-1`), entity assembly, persistence, DTO mapping.
- [ ] Exception handling mappings for `DuplicateEmailException` (`409 Conflict`) and `MethodArgumentNotValidException` (`400 Bad Request`).
- [ ] Traceability matrix back to `US-001` Acceptance Criteria and Business Rules.
- [ ] Explicit reference to Open Decisions `OD-001` through `OD-004`.

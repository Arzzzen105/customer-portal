---
artifact_type: open_decisions
story: US-001
version: 2
status: DRAFT
created_at: 2026-08-31T15:41:30Z
updated_at: 2026-08-31T15:47:00Z
produced_by: us-clarifier
inputs:
  - path: docs/stories/US-001-register-customer.md
    version: null
supersedes: docs/decisions/US-001-open-decisions.md
---

# Open Decisions — US-001: Customer Registration

This document records the design and implementation decisions surfaced during clarification and resolved by the human stakeholder.

---

## OD-001: Email Normalization and Case-Insensitive Storage Strategy

- **Question:** How should email case-insensitivity and uniqueness (per `BR-001` and `BR-002`) be enforced across the API and persistence layers?
- **Context:** `business-rules.md` specifies `BR-001` (Customer email must be unique) and `BR-002` (Customer email comparison is case-insensitive).
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `DB_DESIGN`, `IMPLEMENTATION`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-08-31T15:47:00Z
- **Selected Option:** **Option A**
- **Resolution:** Normalize emails in the Service layer (trim leading/trailing whitespace, convert to lowercase) before duplicate checks and persistence. The database stores the normalized lowercase email with a unique constraint `uq_customer_email`.

---

## OD-002: Customer Registration Endpoint Path

- **Question:** What is the canonical REST endpoint path for customer registration?
- **Context:** `api-conventions.md` (AC-3, AC-4) prescribes plural noun REST endpoints (`POST /api/v1/customers` creates a customer resource).
- **Affects:** `SPECIFICATION`, `API_DESIGN`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-08-31T15:47:00Z
- **Selected Option:** **Option A**
- **Resolution:** `POST /api/v1/customers` — strictly aligns with RESTful resource creation convention in `docs/architecture/api-conventions.md` (AC-3, AC-4).

---

## OD-003: Registration Success Response Payload and Location Header

- **Question:** What attributes should be included in the `201 Created` response DTO upon successful registration?
- **Context:** `US-001` Acceptance Criteria `AC-005` strictly mandates that neither plaintext password nor password hash is returned. `api-conventions.md` AC-4 specifies returning `201 Created`, a `Location` header, and the created resource body.
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `DB_DESIGN`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-08-31T15:47:00Z
- **Selected Option:** **Option A**
- **Resolution:** Return `201 Created` with header `Location: /api/v1/customers/{id}` and response body containing `{ "id": Long, "email": String, "role": String, "createdAt": Instant }`. Sensitive credentials (password, password_hash) are never returned.

---

## OD-004: Duplicate Email Conflict Handling and Error Payload

- **Question:** How should duplicate registration attempts be handled in terms of HTTP status and error response content?
- **Context:** `AC-002` specifies rejection without creating duplicate accounts. `api-conventions.md` AC-5 and AC-6 identify `409 Conflict` for duplicate email conflicts with a structured JSON error body.
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `SECURITY_REVIEW`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-08-31T15:47:00Z
- **Selected Option:** **Option A**
- **Resolution:** Return `HTTP 409 Conflict` with standard error response body (`timestamp`, `status: 409`, `error: "Conflict"`, `message: "An account with this email already exists."`, `path: "/api/v1/customers"`).

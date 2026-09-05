---
artifact_type: open_decisions
story: US-003
version: 2
status: APPROVED
created_at: 2026-09-05T09:58:00Z
updated_at: 2026-09-05T10:04:30Z
produced_by: us-clarifier
inputs:
  - path: docs/stories/US-003-customer-profile-view.md
    version: null
supersedes: docs/decisions/US-003-open-decisions.md
---

# Open Decisions — US-003: View Customer Profile

This document records the decisions surfaced during clarification of US-003 and resolved by the human stakeholder.

---

## OD-001: Profile Endpoint URI and Routing

- **Question:** What is the canonical REST endpoint path and routing design for viewing a customer's profile?
- **Context:**
  - AC-001 requires an authenticated customer to view their profile.
  - AC-002 specifies ownership enforcement ("When another customer's profile is requested, Then access is denied").
  - `api-conventions.md` (AC-3, AC-4) defines resource paths like `GET /api/v1/customers/{id}` for retrieving a single resource.
  - Additionally, some REST APIs provide `GET /api/v1/customers/me` as a convenient shorthand for accessing the current user's profile without knowing their ID in the path.
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `SECURITY_REVIEW`, `IMPLEMENTATION`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-05T10:04:30Z
- **Selected Option:** **Option A**
- **Resolution:** `GET /api/v1/customers/{id}`, where `{id}` must match the authenticated customer's ID.

---

## OD-002: HTTP Status Code and Error Response for Unauthorized Profile Access

- **Question:** What HTTP response status and error payload should be returned when an authenticated customer attempts to access another customer's profile?
- **Context:**
  - AC-002 states: "When another customer's profile is requested, Then access is denied."
  - `api-conventions.md` (AC-5) specifies:
    - `401 Unauthorized` for missing/invalid authentication.
    - `403 Forbidden` for "authenticated but not permitted".
    - `404 Not Found` for "resource does not exist (or is not visible to the caller)".
  - `security-conventions.md` (SC-4) requires: "Ownership checks (a customer may act only on their own resource) are enforced in the Service layer, not just by role."
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `SECURITY_REVIEW`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-05T10:04:30Z
- **Selected Option:** **Option A**
- **Resolution:** Return `HTTP 403 Forbidden` with standard error payload (`message: "Access denied"`), explicitly indicating ownership denial.

---

## OD-003: Profile Response Payload Schema and Fields

- **Question:** What data fields must be included in the profile response payload?
- **Context:**
  - AC-003 states: "Sensitive Data Exclusion: Then the following must not be returned: password, password hash."
  - AC-004 states: "Consistent Response: Then response follows API conventions."
  - In US-001, `CustomerResponse` was defined with `{ id, email, role, createdAt }`.
  - The `Customer` entity in `Customer.java` contains `id`, `email`, `passwordHash`, `role`, `enabled`, `createdAt`, `updatedAt`.
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `IMPLEMENTATION`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-05T10:04:30Z
- **Selected Option:** **Option A**
- **Resolution:** Reuse `CustomerResponse` containing `{ id, email, role, createdAt }`, maintaining consistency across endpoints and strictly excluding credentials.

---

## OD-004: Ownership Verification Precedence vs Resource Existence

- **Question:** Should ownership verification take precedence over database lookup when `{id}` does not belong to the caller?
- **Context:**
  - If a customer requests `/api/v1/customers/999` and their own ID is `1`:
    - Checking ownership first: `id != 1` -> immediately reject with `403 Forbidden` without querying ID 999.
    - Querying DB first: if ID 999 does not exist in DB -> returns `404 Not Found`. If it exists -> returns `403 Forbidden`.
  - Checking ownership before querying avoids unnecessary DB queries and prevents timing attacks/resource existence probing.
- **Affects:** `SPECIFICATION`, `SECURITY_REVIEW`, `IMPLEMENTATION`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-05T10:04:30Z
- **Selected Option:** **Option A**
- **Resolution:** Verify caller ownership against the requested `{id}` first; return `HTTP 403 Forbidden` immediately if `{id}` does not match the authenticated caller's ID before any entity query.

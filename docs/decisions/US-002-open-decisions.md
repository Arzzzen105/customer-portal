---
artifact_type: open_decisions
story: US-002
version: 2
status: DRAFT
created_at: 2026-09-02T13:13:20Z
updated_at: 2026-09-02T13:18:52Z
produced_by: us-clarifier
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
supersedes: docs/decisions/US-002-open-decisions.md
---

# Open Decisions — US-002: Customer Login

This document records the design and implementation decisions surfaced during clarification and resolved by the human stakeholder.

---

## OD-001: Authentication Endpoint Routing and Protocol

- **Question:** What is the canonical REST endpoint path and request protocol for customer authentication?
- **Context:** `api-conventions.md` (AC-3) establishes `/api/v1/...` REST conventions. `security-conventions.md` (SC-3) defines session-based authentication for the initial MVP. Standard REST login is typically `POST /api/v1/auth/login` accepting `application/json`.
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `SECURITY_REVIEW`, `IMPLEMENTATION`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-02T13:18:52Z
- **Selected Option:** **Option A**
- **Resolution:** `POST /api/v1/auth/login` accepting JSON request body `{ "email": "...", "password": "..." }`, establishing a Spring Security session context.

---

## OD-002: Uniform Authentication Failure Response and Anti-Enumeration

- **Question:** How should authentication failures (invalid credentials, non-existent account, disabled account) be reported in HTTP response codes and error payloads?
- **Context:** Acceptance criteria `AC-002` (invalid password), `AC-003` (unknown account), and `AC-004` (disabled account) require authentication failure. `security-conventions.md` (SC-3) mandates: "Failed authentication returns `401` with the standard error body — it does not reveal whether the email exists (no account enumeration)". `BR-004` prevents disabled accounts from authenticating.
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `SECURITY_REVIEW`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-02T13:18:52Z
- **Selected Option:** **Option A**
- **Resolution:** Return `HTTP 401 Unauthorized` with a uniform error payload (`message: "Invalid email or password"`) for wrong password, missing email, and disabled accounts to prevent account enumeration attacks.

---

## OD-003: Login Success Response Payload

- **Question:** What response body structure should be returned upon successful authentication?
- **Context:** `AC-005` strictly mandates that sensitive credentials (passwords, password hashes) are never returned. `api-conventions.md` (AC-4, AC-7) specifies `200 OK` for successful read/action requests, with session cookie established.
- **Affects:** `SPECIFICATION`, `API_DESIGN`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-02T13:18:52Z
- **Selected Option:** **Option A**
- **Resolution:** Return `HTTP 200 OK` with authenticated customer identity response `{ "id": Long, "email": String, "role": String }`. Sensitive credentials (password, password_hash) are never returned.

---

## OD-004: Email Normalization Strategy for Authentication

- **Question:** How should email input be normalized before querying credentials during login?
- **Context:** `BR-002` specifies case-insensitive email comparison. In `US-001`, `OD-001` resolved to trim whitespace and lowercase emails at the Service layer before persistence.
- **Affects:** `SPECIFICATION`, `API_DESIGN`, `IMPLEMENTATION`
- **Status:** `RESOLVED`
- **Decided By:** Human User
- **Decided At:** 2026-09-02T13:18:52Z
- **Selected Option:** **Option A**
- **Resolution:** Normalize login email input by trimming whitespace and converting to lowercase before looking up the customer in `UserDetailsService` / `CustomerRepository`.
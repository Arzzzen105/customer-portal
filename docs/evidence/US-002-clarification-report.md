---
artifact_type: clarification_report
story: US-002
version: 1
status: DRAFT
created_at: 2026-09-02T13:13:20Z
updated_at: 2026-09-02T13:13:20Z
produced_by: us-clarifier
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
supersedes: null
---

# Clarification Report — US-002: Customer Login

## 1. Executive Summary

This report documents the clarification analysis for **US-002: Customer Login**, preparing the story for specification authoring (`spec-writer`). The scope, actors, business rules, acceptance criteria, security requirements, and open decisions have been analyzed against product documentation and architectural conventions.

---

## 2. Business Intent & Context

- **Story ID:** `US-002`
- **Title:** Customer Login
- **Epic:** `EPIC-1`
- **Primary Actor:** Customer (registered user accessing the portal)
- **Business Value:** Allow registered customers to authenticate securely and obtain access to protected portal capabilities.
- **Core Action:** A customer submits their registered email address and plaintext password to authenticate and establish an active session.

---

## 3. Acceptance Criteria Analysis

| Criteria ID | Summary | Clarification & Invariants |
|---|---|---|
| **AC-001** | Successful Login | Valid credentials of an enabled customer authenticate successfully, establishing session context and returning a `200 OK` response. |
| **AC-002** | Invalid Password | An invalid password for an existing customer account results in authentication failure (`401 Unauthorized`). |
| **AC-003** | Unknown Account | An attempt to log in with an email that does not exist results in authentication failure (`401 Unauthorized`). |
| **AC-004** | Disabled Account | An attempt to log in with valid credentials for an account where `enabled = false` fails per `BR-004`. |
| **AC-005** | Secure Authentication Response | Authentication responses must never return plaintext passwords, password hashes, or sensitive internal credentials (`SC-1`, `SC-9`). |

---

## 4. Architectural & Security Alignments

1. **Security Policy Alignment (`security-conventions.md`):**
   - **Authentication Model:** Session-based authentication using Spring Security (`SC-3`).
   - **Credential Verification:** Verification of plaintext password against stored BCrypt hash (`password_hash`) via `PasswordEncoder.matches()` / Spring Security `DaoAuthenticationProvider`.
   - **Account Lookup:** `UserDetailsService` loads customer by email.
   - **Anti-Enumeration:** Uniform `401 Unauthorized` error response for all credential validation failures (unknown user, bad password, disabled account) to prevent account enumeration (`SC-3`).
   - **Public Endpoint:** The login endpoint must be publicly accessible without prior authentication (`SC-4`), permitAll in `SecurityConfig`.

2. **API Conventions Alignment (`api-conventions.md`):**
   - **Path Versioning:** `/api/v1/...`.
   - **Media Type:** `application/json` (`UTF-8`).
   - **HTTP Statuses:** `200 OK` on successful login, `400 Bad Request` on malformed request / validation failure, `401 Unauthorized` on authentication failure.
   - **Error Payload Format:** Strictly follows AC-6 schema (`timestamp`, `status`, `error`, `message`, `path`).

3. **Persistence & Domain Model Alignment (`persistence-conventions.md`):**
   - Customer entity already contains `id`, `email`, `password_hash`, `role`, `enabled`, `created_at`, `updated_at`.
   - No schema changes are required for basic login; persistence model is leveraged as established in US-001.

---

## 5. Ambiguities, Contradictions, and Missing Requirements

1. **Login Endpoint URI & Protocol:** Clarify exact endpoint routing (`POST /api/v1/auth/login`) and request payload schema. Captured as **OD-001**.
2. **Error Message Uniformity (Anti-Enumeration):** Clarify whether disabled accounts return `401 Unauthorized` with generic message or `403 Forbidden`. Captured as **OD-002**.
3. **Login Response Payload Details:** Clarify exact response DTO fields (`id`, `email`, `role`) on `200 OK`. Captured as **OD-003**.
4. **Email Normalization:** Confirm trimming and lowercasing email input during login request processing per `BR-002`. Captured as **OD-004**.

---

## 6. Out of Scope Verification

In alignment with `docs/stories/US-002-customer-login.md`:
- Multi-Factor Authentication (MFA) (`OUT_OF_SCOPE`)
- OAuth2 / Social Login (`OUT_OF_SCOPE`)
- Password Recovery / Reset (`OUT_OF_SCOPE`)
- Remember-Me persistent tokens (`OUT_OF_SCOPE`)
- Session management / concurrent session limits (`OUT_OF_SCOPE`)

---

## 7. Open Decisions Reference

All identified open decisions are formally tracked in `docs/decisions/US-002-open-decisions.md`:

- **OD-001:** Authentication Endpoint Routing and Protocol (Status: `OPEN`)
- **OD-002:** Uniform Authentication Failure Response and Anti-Enumeration (Status: `OPEN`)
- **OD-003:** Login Success Response Payload (Status: `OPEN`)
- **OD-004:** Email Normalization Strategy for Authentication (Status: `OPEN`)

---

## 8. Specification Authoring Checklist (`spec-writer`)

The upcoming Specification (`docs/specifications/US-002-spec.md`) must cover:
- [ ] Detailed functional specification mapping all 5 Acceptance Criteria (`AC-001` through `AC-005`).
- [ ] Concrete endpoint definition (`POST /api/v1/auth/login`), login request payload schema (`LoginRequest`), and login response payload schema (`LoginResponse` / `CustomerResponse`).
- [ ] Bean Validation rules for login request (`email` non-blank/valid email format, `password` non-blank).
- [ ] Spring Security configuration updates: `SecurityFilterChain` permitting login endpoint, `UserDetailsService` / `AuthenticationManager` integration.
- [ ] Authentication failure handling: Spring Security `AuthenticationEntryPoint` / `AuthenticationFailureHandler` / exception translation returning standard `401 Unauthorized` JSON.
- [ ] Traceability matrix back to `US-002` Acceptance Criteria, `BR-002`, `BR-004`, `SC-1`, and `SC-3`.
- [ ] Explicit reference to Open Decisions `OD-001` through `OD-004`.
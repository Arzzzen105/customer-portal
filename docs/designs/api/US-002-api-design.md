---
artifact_type: api_design
story: US-002
version: 1
status: DRAFT
created_at: 2026-09-02T13:25:00Z
updated_at: 2026-09-02T13:25:00Z
produced_by: openapi-designer
inputs:
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/reviews/specifications/US-002-spec-review.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
---

# API Design Notes — US-002: Customer Login

## 1. Rationale & Architecture Alignment

This API design establishes the HTTP contract and interaction model for customer authentication (login), strictly following project conventions:

- **API Conventions (`docs/architecture/api-conventions.md`):**
  - Path versioning: `/api/v1/auth/login` (`AC-1`, `AC-3`, `OD-001` Option A).
  - Method and response: `POST` returning `200 OK` with customer identity details upon successful authentication (`AC-4`, `OD-003` Option A).
  - Media type: `application/json;charset=UTF-8` (`AC-2`).
  - Error format: Structured error model (`AC-6`) with `timestamp`, `status`, `error`, `message`, `path`, and optional `fieldErrors`.
- **Security Conventions (`docs/architecture/security-conventions.md`):**
  - Public access: The authentication endpoint is publicly accessible without prior authentication (`SC-4`, `SEC-001`).
  - Anti-enumeration defense: Authentication failures return uniform `HTTP 401 Unauthorized` with generic message `"Invalid email or password"` across non-existent email, bad password, and disabled accounts (`SC-3`, `OD-002` Option A, `FR-005`, `SEC-004`).
  - Sensitive credential exclusion: Plaintext passwords and password hashes are strictly excluded from response payloads, headers, error models, and logs (`SC-1`, `SC-9`, `AD-4`, `AC-005`).
  - Session establishment: Successful login sets up a Spring Security session context and session cookie (`JSESSIONID`) (`SC-3`, `SEC-005`).
- **Open Decisions Alignment (`docs/decisions/US-002-open-decisions.md`):**
  - `OD-001` Option A: Endpoint route `POST /api/v1/auth/login`.
  - `OD-002` Option A: Uniform `401 Unauthorized` failure response (`"Invalid email or password"`).
  - `OD-003` Option A: Response body containing `id`, `email`, and `role`.
  - `OD-004` Option A: Email normalization (trim + lowercase) at Service layer before database lookup.

The paired OpenAPI 3.0.3 contract is stored in [docs/designs/api/US-002-openapi.yaml](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-002-openapi.yaml).

---

## 2. Operation Details: `POST /api/v1/auth/login`

- **Operation ID:** `login`
- **Summary:** Authenticate customer and establish session
- **Controller Layer:** `org.example.customerportal.controller.AuthController`
- **Request Binding:** `@Valid @RequestBody LoginRequest`
- **Response Type:** `ResponseEntity<LoginResponse>`

### 2.1 Request Schema (`model.request.LoginRequest`)

| Field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `email` | `String` | Yes | `@NotBlank`, `@Email`, `@Size(max = 255)` | Registered customer email address. |
| `password` | `String` | Yes | `@NotBlank`, `@Size(max = 72)` | Customer password submitted for verification. |

*Note on Validation:* Inbound request validation enforces `@NotBlank` and max lengths. Full password complexity validation is enforced during registration (`US-001`); login verifies the password against the stored BCrypt hash via `PasswordEncoder.matches()`.

### 2.2 Response Schema (`model.dto.LoginResponse`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Unique customer surrogate identifier. |
| `email` | `String` | Normalized email address of the authenticated customer. |
| `role` | `String` | Assigned security role (`CUSTOMER` or `ADMIN`). |

---

## 3. Acceptance Criteria to API Operation Mapping

| Story AC ID | AC Title | HTTP Request / Preconditions | Expected HTTP Outcome | Response Body / Headers |
|---|---|---|---|---|
| **AC-001** | Successful Login | `POST /api/v1/auth/login` with valid email & password for enabled account | `200 OK` | `Set-Cookie: JSESSIONID=...`, body `LoginResponse` (`id`, `email`, `role`) |
| **AC-002** | Invalid Password | `POST /api/v1/auth/login` with valid registered email & wrong password | `401 Unauthorized` | Standard error body with uniform message `"Invalid email or password"` |
| **AC-003** | Unknown Account | `POST /api/v1/auth/login` with unregistered email | `401 Unauthorized` | Standard error body with uniform message `"Invalid email or password"` |
| **AC-004** | Disabled Account | `POST /api/v1/auth/login` with valid credentials for disabled account (`enabled = false`) | `401 Unauthorized` | Standard error body with uniform message `"Invalid email or password"` |
| **AC-005** | Secure Response | `POST /api/v1/auth/login` (any successful or failed attempt) | `200` / `4xx` | Neither plaintext password nor hash present in body or headers |

---

## 4. Authentication & Authorization Model

- **Authentication:** Unauthenticated / Public endpoint. Spring Security configuration explicitly permits `POST /api/v1/auth/login`.
- **Session Management:** On successful authentication, Spring Security stores the `Authentication` token in `SecurityContextHolder` and associates it with the `HttpSession`, issuing a `JSESSIONID` cookie (`HttpOnly`).
- **Authorization:** Public access for anonymous users to login; upon authentication, user receives `ROLE_CUSTOMER` or `ROLE_ADMIN` authorities for accessing protected endpoints.

---

## 5. Error Model & Status Codes

All errors conform to `docs/architecture/api-conventions.md` `AC-6`:

1. **`400 Bad Request`:**
   - Triggered by `MethodArgumentNotValidException` (e.g. blank email, invalid email format, blank password) or `HttpMessageNotReadableException` (malformed JSON).
   - Response body includes `fieldErrors: [{"field": "...", "message": "..."}]`.
2. **`401 Unauthorized`:**
   - Triggered by authentication failure (`BadCredentialsException`, `DisabledException`, `UsernameNotFoundException`).
   - Anti-enumeration response body:
     ```json
     {
       "timestamp": "2026-09-02T13:25:00Z",
       "status": 401,
       "error": "Unauthorized",
       "message": "Invalid email or password",
       "path": "/api/v1/auth/login"
     }
     ```
3. **`415 Unsupported Media Type`:**
   - Triggered by requests with missing or non-JSON `Content-Type` header.
4. **`500 Internal Server Error`:**
   - Fallback handler for unhandled exceptions, omitting internal stack traces and technical details.

---

## 6. Compatibility & Pagination Notes

- **Backward Compatibility:** Introduces a new endpoint `/api/v1/auth/login`. The existing customer registration endpoint `POST /api/v1/customers` remains unchanged.
- **Pagination:** Not applicable to single authentication requests.
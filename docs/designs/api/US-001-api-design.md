---
artifact_type: api_design
story: US-001
version: 1
status: DRAFT
created_at: 2026-08-31T15:54:00Z
updated_at: 2026-08-31T15:54:00Z
produced_by: openapi-designer
inputs:
  - path: docs/specifications/US-001-spec.md
    version: 1
  - path: docs/reviews/specifications/US-001-spec-review.md
    version: 1
  - path: docs/decisions/US-001-open-decisions.md
    version: 2
supersedes: null
---

# API Design Notes — US-001: Customer Registration

## 1. Rationale & Architecture Alignment

This API design establishes the HTTP interface for customer registration, strictly adhering to project conventions:
- **API Conventions (`docs/architecture/api-conventions.md`):**
  - Path versioning: `/api/v1/customers` (AC-1, AC-3).
  - Method and response: `POST` returning `201 Created` with a `Location: /api/v1/customers/{id}` header and response body (AC-4).
  - Media type: `application/json;charset=UTF-8` (AC-2).
  - Error format: AC-6 structured error model (`timestamp`, `status`, `error`, `message`, `path`, optional `fieldErrors`).
- **Security Conventions (`docs/architecture/security-conventions.md`):**
  - Public access for registration endpoint without prior authentication (SC-4).
  - Sensitive credential exclusion: Plaintext passwords and password hashes are strictly omitted from response models, error responses, and headers (SC-1, SC-9, AD-4).
- **Open Decisions Alignment (`docs/decisions/US-001-open-decisions.md`):**
  - `OD-002` Option A: Endpoint `/api/v1/customers`.
  - `OD-003` Option A: Response DTO containing `id`, `email`, `role`, `createdAt`.
  - `OD-004` Option A: Duplicate conflict returns `409 Conflict`.

The paired OpenAPI contract is stored in [docs/designs/api/US-001-openapi.yaml](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-001-openapi.yaml).

---

## 2. Operation Details: `POST /api/v1/customers`

- **Operation ID:** `registerCustomer`
- **Summary:** Register a new customer
- **Controller Layer:** `org.example.customerportal.controller.CustomerController`
- **Request Binding:** `@Valid @RequestBody RegisterCustomerRequest`
- **Response Type:** `ResponseEntity<CustomerResponse>`

### 2.1 Request Schema (`model.request.RegisterCustomerRequest`)

| Field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `email` | `String` | Yes | `@NotBlank`, `@Email`, `@Size(max = 255)` | Prospective customer email address. |
| `password` | `String` | Yes | `@NotBlank`, `@Size(min = 12, max = 72)`, `@ValidPassword` | Complex password (min 1 uppercase, 1 lowercase, 1 digit, 1 special char). |

### 2.2 Response Schema (`model.dto.CustomerResponse`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Unique customer surrogate ID. |
| `email` | `String` | Normalized email address. |
| `role` | `String` | Default assigned role (`CUSTOMER`). |
| `createdAt` | `Instant` | Account creation timestamp (UTC ISO-8601). |

---

## 3. Acceptance Criteria to API Operation Mapping

| Story AC ID | AC Title | HTTP Request / Preconditions | Expected HTTP Outcome | Response Body / Headers |
|---|---|---|---|---|
| **AC-001** | Successful Registration | `POST /api/v1/customers` with valid email & password | `201 Created` | Header `Location: /api/v1/customers/{id}`, body `CustomerResponse` |
| **AC-002** | Unique Email | `POST /api/v1/customers` with already registered email | `409 Conflict` | Standard error body with message `"An account with this email already exists."` |
| **AC-003** | Email Validation | `POST /api/v1/customers` with invalid email format / length | `400 Bad Request` | ValidationErrorResponse with `fieldErrors` indicating invalid email |
| **AC-004** | Password Storage | `POST /api/v1/customers` valid request | `201 Created` | Plaintext password never stored; handled internally via BCrypt |
| **AC-005** | Secure Response | `POST /api/v1/customers` any valid/invalid request | `201` / `4xx` | Neither password nor hash present in body or headers |

---

## 4. Authentication & Authorization Model

- **Authentication:** Unauthenticated / Public. Spring Security configuration permits all requests to `POST /api/v1/customers`.
- **Authorization:** Anonymous public user.
- **CSRF:** Evaluated against project security conventions (`security-conventions.md` SC-5). Registration is a public API endpoint.

---

## 5. Error Model & Status Codes

All errors conform to `docs/architecture/api-conventions.md` AC-6:

1. **`400 Bad Request`:**
   - Triggered by `MethodArgumentNotValidException` or `HttpMessageNotReadableException`.
   - Includes `fieldErrors: [{"field": "...", "message": "..."}]`.
2. **`409 Conflict`:**
   - Triggered by `DuplicateEmailException`.
   - Error body: `{"timestamp": "...", "status": 409, "error": "Conflict", "message": "An account with this email already exists.", "path": "/api/v1/customers"}`.
3. **`415 Unsupported Media Type`:**
   - Triggered by requests missing `Content-Type: application/json`.
4. **`500 Internal Server Error`:**
   - Fallback handler for unmapped runtime exceptions; internal stack traces or SQL details are suppressed.

---

## 6. Compatibility & Pagination Notes

- **Initial Release:** This is the baseline endpoint for customer resources. No backward compatibility issues or migrations exist.
- **Pagination:** Not applicable to single-resource creation (`POST /api/v1/customers`).

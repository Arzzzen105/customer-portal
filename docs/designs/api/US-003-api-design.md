---
artifact_type: api_design
story: US-003
version: 1
status: DRAFT
created_at: 2026-09-05T10:05:00Z
updated_at: 2026-09-05T10:05:00Z
produced_by: openapi-designer
inputs:
  - path: docs/specifications/US-003-spec.md
    version: 1
  - path: docs/reviews/specifications/US-003-spec-review.md
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
    version: 2
supersedes: null
---

# API Design Notes — US-003: View Customer Profile

## 1. Rationale & Architecture Alignment

This API design defines the HTTP contract and interaction semantics for viewing customer profile data, adhering strictly to the approved specification and architectural rules:

- **API Conventions (`docs/architecture/api-conventions.md`):**
  - Path Versioning & Resource Naming: Plural noun path segment `/api/v1/customers/{id}` (`AC-1`, `AC-3`, `OD-001` Option A).
  - Method and Response Status: `GET` returning `200 OK` for single-resource retrieval (`AC-4`).
  - Media Type: `application/json` (`AC-2`).
  - Error Payload: Standardized error envelope (`AC-6`) with `timestamp`, `status`, `error`, `message`, and `path`.
- **Security Conventions (`docs/architecture/security-conventions.md`):**
  - Protected Endpoint: Requires active authenticated session context (`SC-4`, `SEC-001`); anonymous requests return `401 Unauthorized`.
  - Ownership Enforcement: Customer isolation is strictly verified at the Service layer (`SC-4`, `SEC-002`, `AC-002`). If an authenticated customer requests another customer's ID, access is denied with `HTTP 403 Forbidden` (`OD-002` Option A).
  - Precedence: Ownership verification precedes repository queries, preventing ID enumeration and unnecessary database roundtrips (`OD-004` Option A).
  - Sensitive Data Exclusion: Neither plaintext passwords nor `passwordHash` are serialized into the response DTO (`SC-1`, `SC-9`, `AC-003`, `OD-003` Option A).
- **Open Decisions Alignment (`docs/decisions/US-003-open-decisions.md`):**
  - `OD-001` Option A: Canonical route `GET /api/v1/customers/{id}`.
  - `OD-002` Option A: Cross-tenant access attempts return `HTTP 403 Forbidden`.
  - `OD-003` Option A: Reuse `CustomerResponse` (`id`, `email`, `role`, `createdAt`).
  - `OD-004` Option A: Verify caller ownership prior to entity lookup.

The paired OpenAPI 3.0.3 contract is stored in [docs/designs/api/US-003-openapi.yaml](file:///d:/Java/SoftServeAgenticAI/Harness/docs/designs/api/US-003-openapi.yaml).

---

## 2. Operation Details: `GET /api/v1/customers/{id}`

- **Operation ID:** `getCustomerProfile`
- **Summary:** Retrieve customer profile by ID
- **Controller Layer:** `org.example.customerportal.controller.CustomerController`
- **Path Parameter Binding:** `@PathVariable("id") Long id`
- **Security Context Binding:** `Authentication authentication` (or `@AuthenticationPrincipal`)
- **Response Type:** `ResponseEntity<CustomerResponse>`

### 2.1 Request Parameters

| Parameter | Location | Type | Required | Constraints | Description |
|---|---|---|---|---|---|
| `id` | `path` | `Long` | Yes | Numeric, positive integer (`id > 0`) | Surrogate customer ID. |

### 2.2 Response Schema (`model.dto.CustomerResponse`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Unique customer surrogate identifier. |
| `email` | `String` | Registered customer email address. |
| `role` | `String` | Security role (`CUSTOMER` or `ADMIN`). |
| `createdAt` | `Instant` | UTC creation timestamp. |

---

## 3. Acceptance Criteria to API Operation Mapping

| Story AC ID | AC Title | HTTP Request & Preconditions | Expected Status | Response Payload |
|---|---|---|---|---|
| **AC-001** | View Own Profile | `GET /api/v1/customers/1` by authenticated customer with ID `1` | `200 OK` | `CustomerResponse` JSON containing `{ id: 1, email: "...", role: "CUSTOMER", createdAt: "..." }` |
| **AC-002** | Ownership Enforcement | `GET /api/v1/customers/2` by authenticated customer with ID `1` | `403 Forbidden` | Standard error body with `error: "Forbidden"`, `message: "Access denied"` |
| **AC-003** | Sensitive Data Exclusion | `GET /api/v1/customers/1` (successful response) | `200 OK` | Neither `password` nor `passwordHash` present in response |
| **AC-004** | Consistent Response | Any request to `GET /api/v1/customers/{id}` | `200` / `4xx` | Conforms to API conventions for success and error bodies |

---

## 4. Authentication & Authorization Model

- **Authentication Model:** Session-based authentication via Spring Security (`SC-3`). The client must supply an active `JSESSIONID` session cookie.
- **Authorization Enforcement:**
  - Spring Security `SecurityFilterChain` default matches `anyRequest().authenticated()`.
  - Service-layer ownership verification:
    ```java
    // Service layer compares authenticated principal with requested ID
    if (!currentUserId.equals(requestedId)) {
        throw new AccessDeniedException("Access denied");
    }
    ```
  - Caller mismatch immediately raises `AccessDeniedException` handled by `GlobalExceptionHandler` to produce `403 Forbidden`.

---

## 5. Error Model & Status Codes

All error bodies strictly follow `docs/architecture/api-conventions.md` `AC-6`:

1. **`400 Bad Request`:**
   - Triggered when `id` cannot be converted to `Long` (e.g. `GET /api/v1/customers/abc`), handled via `MethodArgumentTypeMismatchException`.
2. **`401 Unauthorized`:**
   - Triggered when an unauthenticated client invokes the endpoint without an active session.
3. **`403 Forbidden`:**
   - Triggered when the authenticated customer requests a profile belonging to another customer (`OD-002`, `AC-002`).
   - Example body:
     ```json
     {
       "timestamp": "2026-09-05T10:05:00Z",
       "status": 403,
       "error": "Forbidden",
       "message": "Access denied",
       "path": "/api/v1/customers/2"
     }
     ```
4. **`404 Not Found`:**
   - Triggered when the caller queries their own ID, but no record exists in the database.
   - Example body:
     ```json
     {
       "timestamp": "2026-09-05T10:05:00Z",
       "status": 404,
       "error": "Not Found",
       "message": "Customer not found with id: 1",
       "path": "/api/v1/customers/1"
     }
     ```
5. **`500 Internal Server Error`:**
   - Fallback error response for unanticipated exceptions.

---

## 6. Compatibility & Pagination Notes

- **Backward Compatibility:** Adds a `GET /api/v1/customers/{id}` handler to the existing `CustomerController` (`POST /api/v1/customers` is preserved). Reuses the existing `CustomerResponse` DTO without breaking changes.
- **Pagination:** Not applicable; this operation reads a single customer entity.

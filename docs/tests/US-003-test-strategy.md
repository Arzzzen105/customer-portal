---
artifact_type: test_strategy
story: US-003
version: 1
status: DRAFT
created_at: 2026-09-05T10:24:00Z
updated_at: 2026-09-05T10:24:00Z
produced_by: test-writer
inputs:
  - path: docs/stories/US-003-customer-profile-view.md
    version: null
  - path: docs/specifications/US-003-spec.md
    version: 1
  - path: docs/designs/api/US-003-api-design.md
    version: 1
  - path: docs/designs/api/US-003-openapi.yaml
    version: 1
  - path: docs/designs/database/US-003-db-design.md
    version: 1
  - path: docs/designs/database/US-003-entity-model.md
    version: 1
  - path: docs/impact-analysis/US-003-impact-analysis.md
    version: 1
  - path: docs/plans/US-003-implementation-plan.md
    version: 1
  - path: docs/reviews/plans/US-003-plan-review.md
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
    version: 2
supersedes: null
---

# Test Strategy — US-003: View Customer Profile

## 1. Scope & Objective

This test strategy defines the automated testing approach for **US-003: View Customer Profile**. It establishes rigorous automated verification across all Acceptance Criteria (`AC-001` through `AC-004`), API contract conformance, ownership and tenant isolation constraints, credential privacy (`SC-1`, `SC-9`), and error handling conventions (`AC-6`) before and during production implementation.

---

## 2. Selected Test Levels

1. **End-to-End Integration & Contract Tests (`@SpringBootTest` + `MockMvc`):**
   - Class: `org.example.customerportal.CustomerProfileIntegrationTest`
   - Purpose: Verifies the entire HTTP pipeline, Spring Security filter enforcement (authentication requirement on `GET /api/v1/customers/{id}`), service-layer ownership checks, JSON serialization, status codes (`200`, `400`, `401`, `403`, `404`), credential omission, and database point lookup against the H2 repository.
2. **Web Slice Tests (`@WebMvcTest`):**
   - Class: `org.example.customerportal.controller.CustomerControllerTest` (implemented during production coding)
   - Purpose: Verifies controller layer routing for `GET /api/v1/customers/{id}`, parameter conversion, and type mismatch error handling (`400 Bad Request`).
3. **Unit Tests (JUnit 5 + Mockito):**
   - Class: `org.example.customerportal.service.CustomerServiceTest` (extended during production coding)
   - Purpose: Verifies isolated business logic, caller identity extraction, ownership verification precedence (`OD-004`), missing customer exception throwing (`CustomerNotFoundException`), and DTO projection excluding sensitive credentials.

---

## 3. Test Scenarios by Acceptance Criteria

### AC-001: View Own Profile
- **Positive Scenarios:**
  - Authenticated customer with ID `1` requests `GET /api/v1/customers/1`.
  - Expect `200 OK`, JSON `CustomerResponse` containing `{ "id": 1, "email": "alice@example.com", "role": "CUSTOMER", "createdAt": "..." }`.

### AC-002: Ownership Enforcement
- **Negative & Security Scenarios:**
  - Authenticated customer with ID `1` requests `GET /api/v1/customers/2` (profile belonging to customer 2).
  - Expect `403 Forbidden` with standard error body: `{ "status": 403, "error": "Forbidden", "message": "Access denied", "path": "/api/v1/customers/2" }`.
  - Ownership check must precede database entity lookup (`OD-004` Option A) to prevent ID enumeration and timing side-channels.

### AC-003: Sensitive Data Exclusion
- **Security Scenarios:**
  - On successful profile retrieval (`200 OK`), inspect the response payload.
  - Verify that neither `password` nor `passwordHash` (nor `password_hash`) appears anywhere in the JSON body or response headers (`SC-1`, `SC-9`).

### AC-004: Consistent Response & Error Handling
- **Negative & Boundary Scenarios:**
  - **Unauthenticated Access:** An unauthenticated request to `GET /api/v1/customers/1` without a valid session cookie must return `401 Unauthorized` with an AC-6 compliant error envelope (`message: "Full authentication is required to access this resource"`).
  - **Type Mismatch:** A request with non-numeric ID parameter `GET /api/v1/customers/abc` must return `400 Bad Request` with an AC-6 error envelope.
  - **Resource Not Found:** An authenticated customer requesting their own ID when that record is absent from the database must return `404 Not Found` with an AC-6 error envelope (`message: "Customer not found with id: ..."`).

---

## 4. Test Fixtures & Isolation

- **Database Cleanup:** Each integration test method runs against an isolated, clean H2 in-memory database (`customerRepository.deleteAll()` in `@BeforeEach`).
- **Test Data Seeding:**
  - `Customer 1`: `alice@example.com`, role `CUSTOMER`, active, hashed password.
  - `Customer 2`: `bob@example.com`, role `CUSTOMER`, active, hashed password.
- **Security Mocking:**
  - Integration tests use `SecurityMockMvcRequestPostProcessors.user(email).roles("CUSTOMER")` to simulate authenticated session callers.
  - MockMvc is configured with `SecurityMockMvcConfigurers.springSecurity()`.

---

## 5. Excluded Scenarios & Out-of-Scope Boundaries

- **Profile Updates (`PUT`/`PATCH /api/v1/customers/{id}`):** Explicitly out of scope per User Story.
- **Account Deletion (`DELETE /api/v1/customers/{id}`):** Explicitly out of scope per User Story.
- **Customer Listing / Administration (`GET /api/v1/customers`):** Explicitly out of scope per User Story.

---

## 6. Open Decisions Affecting Testing

- **OD-001 (Endpoint Route):** `GET /api/v1/customers/{id}` is tested.
- **OD-002 (Unauthorized Access Status):** `403 Forbidden` with `message: "Access denied"` is verified.
- **OD-003 (Response Payload):** Reused `CustomerResponse` schema is validated.
- **OD-004 (Check Precedence):** Pre-query ownership validation returning 403 before entity lookup is validated.

All decisions are resolved with human approval.

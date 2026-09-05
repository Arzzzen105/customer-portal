---
artifact_type: ac_test_matrix
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
  - path: docs/plans/US-003-implementation-plan.md
    version: 1
  - path: docs/tests/US-003-test-strategy.md
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
    version: 2
supersedes: null
---

# Acceptance Criteria Test Matrix — US-003: View Customer Profile

This matrix provides complete bidirectional traceability between User Story Acceptance Criteria (`AC-001` through `AC-004`), security/API conventions, and automated test suites.

| Acceptance Criterion | Scenario | Test Level | Test Class | Test Method | Expected Result | Status |
|---|---|---|---|---|---|---|
| **AC-001** (View Own Profile) | Authenticated customer requests own profile ID | Integration | `CustomerProfileIntegrationTest` | `shouldReturnProfileWhenCustomerViewsOwnData` | `200 OK`, `CustomerResponse` JSON (`id`, `email`, `role`, `createdAt`) | RED (Pending Impl) |
| **AC-001** (View Own Profile) | Controller returns 200 with profile DTO | Slice | `CustomerControllerTest` | `shouldReturnCustomerProfileWhenFound` | `200 OK`, `CustomerResponse` payload | Planned |
| **AC-001** (View Own Profile) | Service retrieves caller's profile | Unit | `CustomerServiceTest` | `shouldReturnProfileWhenCallerOwnsResource` | Retrieves customer, maps to DTO excluding credentials | Planned |
| **AC-002** (Ownership Enforcement) | Authenticated customer requests another customer's ID | Integration | `CustomerProfileIntegrationTest` | `shouldReturn403ForbiddenWhenAccessingAnotherCustomerProfile` | `403 Forbidden`, error message `"Access denied"`, AC-6 envelope | RED (Pending Impl) |
| **AC-002** (Ownership Enforcement) | Caller ID mismatch in service layer | Unit | `CustomerServiceTest` | `shouldThrowAccessDeniedWhenCallerDoesNotOwnResource` | Throws `AccessDeniedException("Access denied")` | Planned |
| **AC-003** (Sensitive Data Exclusion) | Profile response body verified for absence of credentials | Integration | `CustomerProfileIntegrationTest` | `shouldNeverExposePasswordDataInProfileResponse` | Neither `password`, `passwordHash`, nor `password_hash` present | RED (Pending Impl) |
| **AC-003** (Sensitive Data Exclusion) | Service-to-DTO mapping credential omission | Unit | `CustomerServiceTest` | `shouldExcludePasswordHashFromCustomerResponse` | DTO contains no credentials | Planned |
| **AC-004** (Consistent Response) | Unauthenticated request to profile endpoint | Integration | `CustomerProfileIntegrationTest` | `shouldReturn401UnauthorizedWhenUnauthenticated` | `401 Unauthorized`, AC-6 envelope (`message: "Full authentication is required..."`) | RED (Pending Impl) |
| **AC-004** (Consistent Response) | Non-numeric path variable `id` | Integration | `CustomerProfileIntegrationTest` | `shouldReturn400BadRequestWhenIdIsNotNumeric` | `400 Bad Request`, AC-6 envelope | RED (Pending Impl) |
| **AC-004** (Consistent Response) | Caller's own customer record missing in database | Integration | `CustomerProfileIntegrationTest` | `shouldReturn404NotFoundWhenOwnProfileDoesNotExist` | `404 Not Found`, AC-6 envelope (`message: "Customer not found with id: ..."`) | RED (Pending Impl) |
| **AC-004** (Consistent Response) | Missing customer in service layer | Unit | `CustomerServiceTest` | `shouldThrowCustomerNotFoundExceptionWhenMissing` | Throws `CustomerNotFoundException` | Planned |

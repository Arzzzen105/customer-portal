---
artifact_type: traceability
story: US-003
version: 1
status: APPROVED
created_at: 2026-09-05T10:41:00Z
updated_at: 2026-09-05T10:41:00Z
produced_by: reconciliation-reviewer
inputs:
  - path: docs/stories/US-003-customer-profile-view.md
    version: null
  - path: docs/specifications/US-003-spec.md
    version: 1
  - path: docs/reviews/specifications/US-003-spec-review.md
    version: 1
  - path: docs/designs/api/US-003-api-design.md
    version: 1
  - path: docs/designs/api/US-003-openapi.yaml
    version: 1
  - path: docs/designs/database/US-003-db-design.md
    version: 1
  - path: docs/designs/database/US-003-entity-model.md
    version: 1
  - path: docs/reviews/designs/US-003-design-review.md
    version: 1
  - path: docs/impact-analysis/US-003-impact-analysis.md
    version: 1
  - path: docs/plans/US-003-implementation-plan.md
    version: 1
  - path: docs/reviews/plans/US-003-plan-review.md
    version: 1
  - path: docs/tests/US-003-test-strategy.md
    version: 1
  - path: docs/tests/US-003-ac-test-matrix.md
    version: 1
  - path: docs/evidence/US-003-test-generation-report.md
    version: 1
  - path: docs/evidence/US-003-implementation-report.md
    version: 1
  - path: docs/verification/US-003-implementation-verification.md
    version: 1
  - path: docs/reviews/security/US-003-security-review.md
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
    version: 2
supersedes: null
reconciled_acceptance_criteria: 4
total_acceptance_criteria: 4
---

# Traceability Matrix — US-003: View Customer Profile

| AC ID | Story Section | Specification Section | Resolved Decision | API Design | DB Design | Impact Analysis | Plan Step | Implementation Location | Test Reference | Verification Evidence | Security Evidence | Final Status |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **AC-001** | View Own Profile | §3 FR-001, FR-002, FR-003, FR-007; §4 AC-001; §7.1 | OD-001, OD-003 | `GET /api/v1/customers/{id}` -> 200 OK (`CustomerResponse`) | `Customer` entity (`id`, `email`, `role`, `created_at`) | `CustomerController.java`, `CustomerService.java` | Step 4, 5 | `CustomerController.getCustomerProfile()`, `CustomerService.getCustomerProfile()` | `CustomerProfileIntegrationTest.shouldReturnProfileWhenCustomerViewsOwnData`, `CustomerControllerTest.shouldReturnCustomerProfileWhenFound`, `CustomerServiceTest.shouldReturnProfileWhenCallerOwnsResource` | PASS | PASS | **RECONCILED** |
| **AC-002** | Ownership Enforcement | §3 FR-003, FR-004; §4 AC-002; §6 SEC-002, SEC-003; §7.2 | OD-002, OD-004 | `GET /api/v1/customers/{id}` -> 403 Forbidden (`ErrorResponse`) | N/A (Service-layer verification) | `CustomerService.java`, `GlobalExceptionHandler.java` | Step 2, 4 | `CustomerService.getCustomerProfile()`, `GlobalExceptionHandler.handleAccessDeniedException()` | `CustomerProfileIntegrationTest.shouldReturn403ForbiddenWhenAccessingAnotherCustomerProfile`, `CustomerServiceTest.shouldThrowAccessDeniedWhenCallerDoesNotOwnResource` | PASS | PASS | **RECONCILED** |
| **AC-003** | Sensitive Data Exclusion | §3 FR-006; §4 AC-003; §6 SEC-004; §7.1 | OD-003 | `CustomerResponse` (excludes credentials) | `Customer` entity `password_hash` column | `CustomerResponse.java`, `CustomerService.java` | Step 4 | `CustomerService.getCustomerProfile()` (DTO projection to `CustomerResponse`) | `CustomerProfileIntegrationTest.shouldNeverExposePasswordDataInProfileResponse`, `CustomerServiceTest.shouldReturnProfileWhenCallerOwnsResource` | PASS | PASS | **RECONCILED** |
| **AC-004** | Consistent Response | §3 FR-001, FR-002, FR-004, FR-005, FR-007; §4 AC-004; §5.1, §5.2; §6 SEC-005; §7.1, §7.2 | OD-001, OD-002, OD-003, OD-004 | `api-conventions.md` AC-6 JSON error format (400, 401, 403, 404) | N/A | `SecurityConfig.java`, `GlobalExceptionHandler.java` | Step 2, 3 | `SecurityConfig` (`AuthenticationEntryPoint`), `GlobalExceptionHandler` (400, 403, 404 handlers) | `CustomerProfileIntegrationTest.shouldReturn401UnauthorizedWhenUnauthenticated`, `CustomerProfileIntegrationTest.shouldReturn400BadRequestWhenIdIsNotNumeric`, `CustomerProfileIntegrationTest.shouldReturn404NotFoundWhenOwnProfileDoesNotExist`, `CustomerControllerTest.shouldReturn400WhenPathVariableIsInvalid` | PASS | PASS | **RECONCILED** |

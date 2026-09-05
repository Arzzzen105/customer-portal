---
artifact_type: traceability
story: US-002
version: 1
status: APPROVED
created_at: 2026-09-02T16:56:00Z
updated_at: 2026-09-02T16:56:00Z
produced_by: reconciliation-reviewer
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/reviews/specifications/US-002-spec-review.md
    version: 1
  - path: docs/designs/api/US-002-api-design.md
    version: 1
  - path: docs/designs/api/US-002-openapi.yaml
    version: 1
  - path: docs/designs/database/US-002-db-design.md
    version: 1
  - path: docs/designs/database/US-002-entity-model.md
    version: 1
  - path: docs/reviews/designs/US-002-design-review.md
    version: 1
  - path: docs/impact-analysis/US-002-impact-analysis.md
    version: 1
  - path: docs/plans/US-002-implementation-plan.md
    version: 1
  - path: docs/reviews/plans/US-002-plan-review.md
    version: 1
  - path: docs/tests/US-002-test-strategy.md
    version: 1
  - path: docs/tests/US-002-ac-test-matrix.md
    version: 1
  - path: docs/evidence/US-002-test-generation-report.md
    version: 1
  - path: docs/evidence/US-002-implementation-report.md
    version: 1
  - path: docs/verification/US-002-implementation-verification.md
    version: 1
  - path: docs/reviews/security/US-002-security-review.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
reconciled_acceptance_criteria: 5
total_acceptance_criteria: 5
---

# Traceability Matrix — US-002: Customer Login

| AC ID | Story Section | Specification Section | Resolved Decision | API Design | DB Design | Impact Analysis | Plan Step | Implementation Location | Test Reference | Verification Evidence | Security Evidence | Final Status |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **AC-001** | Successful Login | `AuthService` logic | OD-001, OD-004 | `POST /api/v1/auth/login` | N/A | `AuthService.java` | Step 4, 5 | `AuthService.login()`, `AuthController.login()` | `CustomerLoginIntegrationTest.shouldAuthenticateCustomerSuccessfully` | PASS | PASS | **RECONCILED** |
| **AC-002** | Invalid Password | `AuthService` failure handling | OD-002 | `401 Unauthorized` | N/A | `AuthService.java`, `GlobalExceptionHandler.java` | Step 3, 4 | `AuthService.login()`, `GlobalExceptionHandler` | `CustomerLoginIntegrationTest.shouldRejectInvalidPassword` | PASS | PASS | **RECONCILED** |
| **AC-003** | Unknown Account | `AuthService` failure handling | OD-002 | `401 Unauthorized` | N/A | `AuthService.java`, `GlobalExceptionHandler.java` | Step 3, 4 | `AuthService.login()`, `GlobalExceptionHandler` | `CustomerLoginIntegrationTest.shouldRejectNonExistentAccount` | PASS | PASS | **RECONCILED** |
| **AC-004** | Disabled Account | `AuthService` failure handling | OD-002 | `401 Unauthorized` | N/A | `AuthService.java`, `GlobalExceptionHandler.java` | Step 3, 4 | `AuthService.login()`, `GlobalExceptionHandler` | `CustomerLoginIntegrationTest.shouldRejectDisabledAccountLogin` | PASS | PASS | **RECONCILED** |
| **AC-005** | Secure Response | `LoginResponse` mapping | OD-003 | `200 OK` Schema | N/A | `LoginResponse.java` | Step 2, 4 | `LoginResponse`, `AuthService.login()` | `CustomerLoginIntegrationTest.shouldNeverExposePasswordData` | PASS | PASS | **RECONCILED** |

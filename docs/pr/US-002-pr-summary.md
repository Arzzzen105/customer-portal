---
artifact_type: pr_summary
story: US-002
version: 1
status: APPROVED
produced_by: pr-preparer
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/impact-analysis/US-002-impact-analysis.md
    version: 1
  - path: docs/plans/US-002-implementation-plan.md
    version: 1
  - path: docs/evidence/US-002-implementation-report.md
    version: 1
  - path: docs/verification/US-002-implementation-verification.md
    version: 1
  - path: docs/reviews/security/US-002-security-review.md
    version: 1
  - path: docs/reviews/reconciliation/US-002-reconciliation.md
    version: 1
  - path: docs/reconciliation/US-002-traceability.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
---

# Pull Request Summary — US-002: Customer Login

## Story & Business Goal
- **Story:** As a Customer I want to authenticate using my email and password So that I can access protected functionality.
- **Business Goal:** Allow registered customers to access the portal securely.

## Scope / Implemented Features
- New authentication endpoint (`POST /api/v1/auth/login`).
- Email normalization and robust password validation.
- Centralized exception handling to ensure consistent `401 Unauthorized` responses on authentication failure.
- Configured Spring Security to permit public access to the login endpoint.

## API Changes
- **Added:** `POST /api/v1/auth/login`
- **Request:** Accepts `LoginRequest` containing `email` and `password`.
- **Response:**
  - `200 OK`: Returns `LoginResponse` (`id`, `email`, `role`).
  - `401 Unauthorized`: Uniform response for invalid credentials, unknown account, or disabled account.

## Database Changes
- **None.** The implementation adheres to a strict zero-migration constraint. The existing `Customer` entity and schema are reused.

## Security Changes
- **BCrypt Password Verification:** Password matching rigorously relies on `BCryptPasswordEncoder.matches()`.
- **Anti-Enumeration Protection:** Returns uniform `401 Unauthorized` with the generic message `"Invalid email or password"` in all failure scenarios.
- **Zero Credential Exposure:** Plaintext passwords and hashes are strictly excluded from response payloads, logs, and exception metadata.

## Tests Executed
- Build completed successfully: `mvn clean test` (Exit code: 0).
- **35 tests passed** across unit and integration suites (0 failures, 0 errors, 0 skipped).
- References:
  - `docs/verification/US-002-implementation-verification.md`
  - `docs/reviews/security/US-002-security-review.md`

## Acceptance Criteria Coverage
From `docs/reconciliation/US-002-traceability.md`:
- **AC-001 Successful Login:** RECONCILED
- **AC-002 Invalid Password:** RECONCILED
- **AC-003 Unknown Account:** RECONCILED
- **AC-004 Disabled Account:** RECONCILED
- **AC-005 Secure Authentication Response:** RECONCILED
- *Total Coverage: 5 of 5 Acceptance Criteria.*

## PR Candidate Files — Include
From `docs/reviews/reconciliation/US-002-reconciliation.md`:
1. `src/main/java/org/example/customerportal/controller/AuthController.java`
2. `src/main/java/org/example/customerportal/service/AuthService.java`
3. `src/main/java/org/example/customerportal/model/request/LoginRequest.java`
4. `src/main/java/org/example/customerportal/model/dto/LoginResponse.java`
5. `src/test/java/org/example/customerportal/controller/AuthControllerTest.java`
6. `src/test/java/org/example/customerportal/service/AuthServiceTest.java`
7. `src/test/java/org/example/customerportal/CustomerLoginIntegrationTest.java`
8. `src/main/java/org/example/customerportal/security/SecurityConfig.java`
9. `src/main/java/org/example/customerportal/exception/GlobalExceptionHandler.java`

## PR Candidate Files — Exclude
- **Runtime Artifacts:** Database files in `data/`
- **Local Configuration:** IDE specific configs
- **Unrelated Changes:** Workflow state files (`docs/workflow/active-story.yaml`, etc.)

## Files Needing Human Decision
- None.

## Risks & Known Limitations
- The provided reviews are based on static analysis and automated test execution. Manual browser session validation and UI acceptance testing should be covered during or post PR review.

## Release Notes
- **User-Visible Changes:** Registered customers can now authenticate securely using their email and password.
- **Technical Changes:** Added `/api/v1/auth/login` endpoint and configured Spring Security for auth processing.
- **Security Notes:** Enabled BCrypt credential validation, uniform generic error messages for anti-enumeration, and zero-exposure DTO mapping.

## Notes For Reviewers
- Review `AuthController.java` and `AuthService.java` for accurate business logic handling and test alignments.
- Validate `GlobalExceptionHandler.java` correctly suppresses and uniformizes authentication exceptions.
- Confirm zero-migration constraints remain unaltered.

## Readiness Result
**PASS**
- All required artifacts are current and aligned.
- All Acceptance Criteria are reconciled.
- PR candidate file scope is isolated securely.

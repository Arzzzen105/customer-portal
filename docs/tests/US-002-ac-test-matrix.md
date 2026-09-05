---
artifact_type: ac_test_matrix
story: US-002
version: 1
status: DRAFT
created_at: 2026-09-02T13:38:00Z
updated_at: 2026-09-02T13:38:00Z
produced_by: test-writer
inputs:
  - path: docs/stories/US-002-customer-login.md
    version: null
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/designs/api/US-002-api-design.md
    version: 1
  - path: docs/designs/api/US-002-openapi.yaml
    version: 1
  - path: docs/designs/database/US-002-db-design.md
    version: 1
  - path: docs/plans/US-002-implementation-plan.md
    version: 1
  - path: docs/tests/US-002-test-strategy.md
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
---

# Acceptance Criteria Test Matrix — US-002: Customer Login

This matrix provides complete bidirectional traceability between User Story Acceptance Criteria (`AC-001` through `AC-005`), related NFRs/validation rules, and automated test suites.

| Acceptance Criterion | Scenario | Test Level | Test Class | Test Method | Expected Result | Status |
|---|---|---|---|---|---|---|
| **AC-001** (Successful Login) | Valid credentials submitted | Integration | `CustomerLoginIntegrationTest` | `shouldAuthenticateCustomerSuccessfully` | `200 OK`, `LoginResponse` (`id`, `email`, `role`) | RED (Pending Impl) |
| **AC-001** (Successful Login) | Email normalization with whitespace and mixed casing | Integration | `CustomerLoginIntegrationTest` | `shouldNormalizeEmailOnLogin` | `200 OK`, matched normalized customer | RED (Pending Impl) |
| **AC-001** (Successful Login) | Service authentication logic | Unit | `AuthServiceTest` | `shouldAuthenticateCustomerSuccessfully` | Validates BCrypt match, returns `LoginResponse` | Planned |
| **AC-002** (Invalid Password) | Wrong password submitted | Integration | `CustomerLoginIntegrationTest` | `shouldRejectInvalidPassword` | `401 Unauthorized`, uniform `"Invalid email or password"` | RED (Pending Impl) |
| **AC-002** (Invalid Password) | Password mismatch in service | Unit | `AuthServiceTest` | `shouldThrowBadCredentialsOnPasswordMismatch` | Throws `BadCredentialsException` | Planned |
| **AC-003** (Unknown Account) | Unregistered email submitted | Integration | `CustomerLoginIntegrationTest` | `shouldRejectNonExistentAccount` | `401 Unauthorized`, uniform `"Invalid email or password"` | RED (Pending Impl) |
| **AC-003** (Unknown Account) | Non-existent customer in service | Unit | `AuthServiceTest` | `shouldThrowBadCredentialsOnMissingAccount` | Throws `BadCredentialsException` | Planned |
| **AC-004** (Disabled Account) | Disabled customer submitted | Integration | `CustomerLoginIntegrationTest` | `shouldRejectDisabledAccountLogin` | `401 Unauthorized`, uniform `"Invalid email or password"` | RED (Pending Impl) |
| **AC-004** (Disabled Account) | Disabled check in service | Unit | `AuthServiceTest` | `shouldThrowBadCredentialsOnDisabledAccount` | Throws `BadCredentialsException` | Planned |
| **AC-005** (Secure Response) | Credential exclusion from response payload | Integration | `CustomerLoginIntegrationTest` | `shouldNeverExposePasswordData` | `password` and `passwordHash` absent | RED (Pending Impl) |
| **NFR-002** (Validation) | Blank email submitted | Integration | `CustomerLoginIntegrationTest` | `shouldRejectBlankEmail` | `400 Bad Request`, `fieldErrors` array | RED (Pending Impl) |
| **NFR-002** (Validation) | Invalid email format submitted | Integration | `CustomerLoginIntegrationTest` | `shouldRejectInvalidEmailFormat` | `400 Bad Request`, `fieldErrors` array | RED (Pending Impl) |
| **NFR-002** (Validation) | Blank password submitted | Integration | `CustomerLoginIntegrationTest` | `shouldRejectBlankPassword` | `400 Bad Request`, `fieldErrors` array | RED (Pending Impl) |
| **NFR-003** (Media Type) | Non-JSON Content-Type submitted | Integration | `CustomerLoginIntegrationTest` | `shouldRejectUnsupportedMediaType` | `415 Unsupported Media Type` | RED (Pending Impl) |

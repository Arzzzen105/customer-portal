---
artifact_type: ac_test_matrix
story: US-001
version: 1
status: DRAFT
created_at: 2026-08-31T16:11:40Z
updated_at: 2026-08-31T16:11:40Z
produced_by: test-writer
inputs:
  - path: docs/stories/US-001-register-customer.md
    version: null
  - path: docs/specifications/US-001-spec.md
    version: 1
  - path: docs/designs/api/US-001-api-design.md
    version: 1
  - path: docs/designs/api/US-001-openapi.yaml
    version: 1
  - path: docs/designs/database/US-001-db-design.md
    version: 1
  - path: docs/plans/US-001-implementation-plan.md
    version: 1
  - path: docs/tests/US-001-test-strategy.md
    version: 1
supersedes: null
---

# Acceptance Criteria Test Matrix — US-001: Customer Registration

This matrix provides full bidirectional traceability between the User Story Acceptance Criteria (`AC-001` through `AC-005`) and the automated test suites.

| Acceptance Criterion | Scenario | Test Level | Test Class | Test Method | Expected Result | Status |
|---|---|---|---|---|---|---|
| **AC-001** (Successful Registration) | Valid email and password submitted | Integration | `CustomerRegistrationIntegrationTest` | `shouldRegisterNewCustomerSuccessfully` | `201 Created`, `Location` header, `CustomerResponse` payload | RED (Pending Impl) |
| **AC-001** (Successful Registration) | Registration business logic | Unit | `CustomerServiceTest` | `shouldRegisterCustomerSuccessfully` | Normalized email, BCrypt hash created, Customer saved | Planned |
| **AC-002** (Unique Email) | Duplicate email registration attempt (case-insensitive) | Integration | `CustomerRegistrationIntegrationTest` | `shouldRejectDuplicateEmailRegistration` | `409 Conflict`, error JSON body with AC-6 format | RED (Pending Impl) |
| **AC-002** (Unique Email) | Database unique constraint enforcement | Data Slice | `CustomerRepositoryTest` | `shouldEnforceUniqueEmailConstraint` | `DataIntegrityViolationException` on duplicate | Planned |
| **AC-003** (Email Validation) | Invalid email format | Integration | `CustomerRegistrationIntegrationTest` | `shouldRejectInvalidEmailFormat` | `400 Bad Request`, `fieldErrors` array | RED (Pending Impl) |
| **AC-003** (Email Validation) | Blank email submitted | Integration | `CustomerRegistrationIntegrationTest` | `shouldRejectBlankEmail` | `400 Bad Request`, `fieldErrors` array | RED (Pending Impl) |
| **AC-003** (Password Validation) | Password length < 12 | Integration | `CustomerRegistrationIntegrationTest` | `shouldRejectShortPassword` | `400 Bad Request`, `fieldErrors` array | RED (Pending Impl) |
| **AC-003** (Password Validation) | Password missing complexity | Integration | `CustomerRegistrationIntegrationTest` | `shouldRejectWeakPassword` | `400 Bad Request`, `fieldErrors` array | RED (Pending Impl) |
| **AC-003** (Password Validation) | Password complexity validator unit tests | Unit | `PasswordValidatorTest` | `shouldValidatePasswordComplexityScenarios` | True for valid, false for invalid complexity | Planned |
| **AC-004** (Password Storage) | Password not stored in plaintext | Integration | `CustomerRegistrationIntegrationTest` | `shouldNeverExposePasswordDataInResponse` | Hash in DB, plaintext absent | RED (Pending Impl) |
| **AC-005** (Secure Response) | Response excludes password and hash | Integration | `CustomerRegistrationIntegrationTest` | `shouldNeverExposePasswordDataInResponse` | `password` and `passwordHash` absent from JSON | RED (Pending Impl) |
| **NFR-003** (API Media Type) | Non-JSON Content-Type submitted | Integration | `CustomerRegistrationIntegrationTest` | `shouldRejectUnsupportedMediaType` | `415 Unsupported Media Type` | RED (Pending Impl) |

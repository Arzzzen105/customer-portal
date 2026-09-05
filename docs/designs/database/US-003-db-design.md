---
artifact_type: database_design
story: US-003
version: 1
status: DRAFT
created_at: 2026-09-05T10:07:00Z
updated_at: 2026-09-05T10:07:00Z
produced_by: db-designer
inputs:
  - path: docs/specifications/US-003-spec.md
    version: 1
  - path: docs/designs/api/US-003-api-design.md
    version: 1
  - path: docs/designs/api/US-003-openapi.yaml
    version: 1
  - path: docs/decisions/US-003-open-decisions.md
    version: 2
supersedes: null
---

# Database Design — US-003: View Customer Profile

## 1. Overview & Architectural Alignment

This database design establishes the persistence model utilized for customer profile retrieval in US-003. Per the approved Specification (`docs/specifications/US-003-spec.md` §8) and API Design (`docs/designs/api/US-003-api-design.md`), US-003 performs read-only querying of existing customer records without altering the database schema or introducing new tables.

Adherence to project persistence conventions (`docs/architecture/persistence-conventions.md`):
- **PC-1 (Database Engine):** H2 database engine (file-based in `./data/customer-portal` locally; isolated in-memory for automated tests).
- **PC-2 (Schema Initialization):** Explicit schema defined in `src/main/resources/schema.sql`; Hibernate `ddl-auto` set to `validate`. No schema changes required for US-003.
- **PC-3 (Identifiers):** Lookup by surrogate primary key `id` (`BIGINT` / `Long`).
- **PC-4 (Explicit Column Mapping):** Explicit non-nullability, types, and column mappings on the existing entity.
- **PC-5 (Naming):** Singular table name `customer`, primary key constraint `pk_customer`.
- **PC-6 (Audit Timestamps):** Reads audit timestamp `created_at` in UTC.
- **PC-7 (Indexes):** Primary key index on `id` satisfies $O(1)$ point lookup requirements for profile queries.
- **PC-9 (Sensitive Data):** `password_hash` column is never exposed to API layers or serialized in profile responses.

---

## 2. Table Definition: `customer`

The table definition below represents the active schema in `src/main/resources/schema.sql` accessed during profile retrieval:

| Column Name | SQL Data Type | Nullable | Unique | Default | Description |
|---|---|---|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | `NO` | `YES (PK)` | Auto | Surrogate primary key; requested path parameter and mapped to `CustomerResponse.id`. |
| `email` | `VARCHAR(255)` | `NO` | `YES` | - | Registered customer email address; mapped to `CustomerResponse.email`. |
| `password_hash` | `VARCHAR(60)` | `NO` | `NO` | - | BCrypt password hash; strictly excluded from profile DTO (`AC-003`, `SC-1`, `SC-9`). |
| `role` | `VARCHAR(20)` | `NO` | `NO` | `'CUSTOMER'` | User authorization role; mapped to `CustomerResponse.role`. |
| `enabled` | `BOOLEAN` | `NO` | `NO` | `TRUE` | Account enabled flag; internal state. |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NO` | `NO` | - | UTC creation timestamp; mapped to `CustomerResponse.createdAt`. |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NO` | `NO` | - | UTC modification timestamp; internal metadata. |

---

## 3. Constraints & Indexes

1. **Primary Key Constraint:**
   - Constraint name: `pk_customer`
   - Target column: `id`
   - Index type: B-Tree primary index automatically created by H2.
   - Purpose: Direct point lookup for `CustomerRepository.findById(id)` providing optimal $O(1)$ query execution.
2. **Unique Constraints & Indexes:**
   - Constraint name: `uq_customer_email`
   - Target column: `email`
   - Index type: Unique B-Tree index (used during authentication and user lookups).

---

## 4. Sensitive Data Handling

- **`password_hash` Exclusion:**
  - Although stored in the `customer` table, `password_hash` is strictly ignored by the mapping to `CustomerResponse`.
  - Service-to-DTO conversion never reads or copies `passwordHash` for profile retrieval (`AC-003`, `SC-1`, `SC-9`).
- **PII Protection:**
  - The customer's email is returned only to the authenticated owner of the profile, enforced by the Service layer prior to DTO delivery (`OD-002`, `OD-004`).

---

## 5. Explicit Schema DDL (`src/main/resources/schema.sql`)

The active DDL in `src/main/resources/schema.sql` supports US-003 without modification:

```sql
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_customer PRIMARY KEY (id),
    CONSTRAINT uq_customer_email UNIQUE (email)
);
```

---

## 6. Persistence Operations for Profile Retrieval

During the execution of `GET /api/v1/customers/{id}`:
1. **Ownership Validation (Pre-persistence):**
   - The Service layer verifies that caller identity matches `{id}` *before* querying the database (`OD-004` Option A).
2. **Customer Point Lookup:**
   - Query: `SELECT * FROM customer WHERE id = :id` executed via `CustomerRepository.findById(id)`.
   - If present: returns `Optional<Customer>` mapped to `CustomerResponse`.
   - If empty: triggers `CustomerNotFoundException` translated to `404 Not Found`.
3. **Transaction Semantics:**
   - The operation is strictly read-only (`@Transactional(readOnly = true)`), guaranteeing zero database mutations, no lock contention, and optimal cache utilization.

---
artifact_type: database_design
story: US-002
version: 1
status: DRAFT
created_at: 2026-09-02T13:27:00Z
updated_at: 2026-09-02T13:27:00Z
produced_by: db-designer
inputs:
  - path: docs/specifications/US-002-spec.md
    version: 1
  - path: docs/designs/api/US-002-api-design.md
    version: 1
  - path: docs/designs/api/US-002-openapi.yaml
    version: 1
  - path: docs/decisions/US-002-open-decisions.md
    version: 2
supersedes: null
---

# Database Design — US-002: Customer Login

## 1. Overview & Architectural Alignment

This database design evaluates and establishes the persistence model required for customer authentication (login). In accordance with the approved Specification (`docs/specifications/US-002-spec.md` §8), US-002 reuses the relational schema established in US-001 without requiring new tables or schema migrations.

Adherence to project persistence conventions (`docs/architecture/persistence-conventions.md`):
- **PC-1 (Database Engine):** H2 database engine (file-based in `./data/customer-portal` locally; isolated in-memory for automated tests).
- **PC-2 (Schema Initialization):** Explicit schema defined in `src/main/resources/schema.sql`; Hibernate `ddl-auto` set to `validate`. No schema change is required for US-002.
- **PC-3 (Identifiers):** Surrogate primary key `id` (`BIGINT AUTO_INCREMENT` / `Long`).
- **PC-4 (Explicit Column Mapping):** Non-nullability, explicit data types, and length constraints on all columns.
- **PC-5 (Naming):** Singular table name `customer`, constraint names `pk_customer` and `uq_customer_email`.
- **PC-6 (Audit Timestamps):** UTC timestamps `created_at` and `updated_at`.
- **PC-7 (Indexes):** Unique index `uq_customer_email` on `customer.email` satisfies lookup index requirements for authentication queries.
- **PC-9 (Sensitive Data):** `password_hash` (`VARCHAR(60)`) stores BCrypt hash exclusively; plaintext passwords are never persisted.

---

## 2. Table Definition: `customer`

The table definition below represents the active schema in `src/main/resources/schema.sql` utilized during authentication:

| Column Name | SQL Data Type | Nullable | Unique | Default | Description |
|---|---|---|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | `NO` | `YES (PK)` | Auto | Surrogate primary key; mapped to `id` in `LoginResponse`. |
| `email` | `VARCHAR(255)` | `NO` | `YES` | - | Normalized customer email address; primary authentication lookup key. |
| `password_hash` | `VARCHAR(60)` | `NO` | `NO` | - | BCrypt password hash (60 chars) verified via `PasswordEncoder.matches()`. |
| `role` | `VARCHAR(20)` | `NO` | `NO` | `'CUSTOMER'` | User authorization role; mapped to granted authorities (`ROLE_CUSTOMER`, `ROLE_ADMIN`). |
| `enabled` | `BOOLEAN` | `NO` | `NO` | `TRUE` | Account enabled status; evaluated during authentication (`BR-004`, `AC-004`). |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NO` | `NO` | - | UTC creation timestamp (managed via JPA Auditing). |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NO` | `NO` | - | UTC last modification timestamp (managed via JPA Auditing). |

---

## 3. Constraints & Indexes

1. **Primary Key:**
   - Constraint name: `pk_customer`
   - Target column: `id`
2. **Unique Constraints & Indexes:**
   - Constraint name: `uq_customer_email`
   - Target column: `email`
   - Purpose: Enforces uniqueness per `BR-001` and serves as the B-tree index for `CustomerRepository.findByEmail(normalizedEmail)` query optimization (`PC-7`).

---

## 4. Sensitive Data Handling

- **`password_hash`:**
  - Length: `VARCHAR(60)` exactly accommodating standard BCrypt hash output.
  - Usage: Loaded into memory solely for matching with incoming plaintext password via `BCryptPasswordEncoder.matches()`. Never returned to clients or logged (`SC-1`, `SC-9`, `AD-4`).
- **PII:**
  - `email` is sensitive identity information, indexed for exact matching and returned in the authenticated `LoginResponse`.

---

## 5. Explicit Schema DDL (`src/main/resources/schema.sql`)

The existing schema in `src/main/resources/schema.sql` is fully compatible with US-002:

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

## 6. Persistence Operations for Authentication

During the execution of `POST /api/v1/auth/login`:
1. **Customer Lookup:** Query `SELECT * FROM customer WHERE email = :email` via `CustomerRepository.findByEmail(normalizedEmail)`.
2. **Account Status Inspection:** Verify `customer.isEnabled() == true`.
3. **Password Verification:** Compare input password against `customer.getPasswordHash()` via `passwordEncoder.matches()`.
4. **No Mutation:** The authentication flow is read-only regarding persistence state (no database updates during basic login).
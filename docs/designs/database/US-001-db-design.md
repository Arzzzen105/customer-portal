---
artifact_type: database_design
story: US-001
version: 1
status: DRAFT
created_at: 2026-08-31T15:56:30Z
updated_at: 2026-08-31T15:56:30Z
produced_by: db-designer
inputs:
  - path: docs/specifications/US-001-spec.md
    version: 1
  - path: docs/designs/api/US-001-api-design.md
    version: 1
  - path: docs/designs/api/US-001-openapi.yaml
    version: 1
  - path: docs/decisions/US-001-open-decisions.md
    version: 2
supersedes: null
---

# Database Design — US-001: Customer Registration

## 1. Overview & Architectural Alignment

This database design defines the relational persistence schema for Customer Registration, strictly adhering to project conventions:
- **Persistence Conventions (`docs/architecture/persistence-conventions.md`):**
  - PC-1: H2 database engine (file-based locally, in-memory for testing).
  - PC-2: Explicit DDL defined for `src/main/resources/schema.sql`; Hibernate `ddl-auto` set to `validate`.
  - PC-3: Surrogate identity primary key (`BIGINT` / `Long`, `GenerationType.IDENTITY`).
  - PC-4: Explicit column mapping with non-nullability, length, and uniqueness constraints on all fields.
  - PC-5: Naming conventions: singular snake_case table name (`customer`), constraint names (`pk_customer`, `uq_customer_email`).
  - PC-6: UTC audit timestamps (`created_at`, `updated_at`).
  - PC-9: Sensitive data: password stored exclusively as BCrypt hash in `password_hash` (`VARCHAR(60)`).
- **Open Decisions Alignment (`docs/decisions/US-001-open-decisions.md`):**
  - `OD-001` Option A: Lowercase normalized email persisted with unique constraint `uq_customer_email`.

---

## 2. Table Definition: `customer`

| Column Name | SQL Data Type | Nullable | Unique | Default | Description |
|---|---|---|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | `NO` | `YES (PK)` | Auto | Surrogate primary key. |
| `email` | `VARCHAR(255)` | `NO` | `YES` | - | Normalized (lowercase, trimmed) customer email address. |
| `password_hash` | `VARCHAR(60)` | `NO` | `NO` | - | Standard BCrypt password hash (60 characters). |
| `role` | `VARCHAR(20)` | `NO` | `NO` | `'CUSTOMER'` | User authorization role (default `CUSTOMER`). |
| `enabled` | `BOOLEAN` | `NO` | `NO` | `TRUE` | Account active / enabled status flag. |
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
   - Purpose: Enforces uniqueness per `BR-001` and provides indexing for repository `findByEmail` / `existsByEmail` queries (`PC-7`).

---

## 4. Sensitive Data Handling

- **`password_hash`:**
  - Length: `VARCHAR(60)` exactly accommodating standard BCrypt hash output.
  - Plaintext password values are never persisted or logged (`SC-1`, `SC-9`, `PC-9`).
- **PII:**
  - `email` is customer identifiable information; it is protected under standard access controls and omitted from debug logs.

---

## 5. Explicit Schema DDL (`src/main/resources/schema.sql`)

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

## 6. Schema Initialization & Migration Strategy

- Hand-maintained explicit schema in `src/main/resources/schema.sql` (`PC-2`).
- Spring Boot SQL init configuration: `spring.sql.init.mode=always`.
- Hibernate validation: `spring.jpa.hibernate.ddl-auto=validate`.

---
artifact_type: entity_model
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

# Entity Model — US-002: Customer Login

## 1. Domain Concept to Entity Mapping

| Business Concept (`business-glossary.md`) | Entity Class | Responsibility & Representation in Authentication |
|---|---|---|
| **Customer** / **Account** | `org.example.customerportal.model.entity.Customer` | Persistent identity holding credentials (`passwordHash`), role, and enabled status for authentication. |

---

## 2. Entity Specification: `Customer`

- **Package:** `org.example.customerportal.model.entity`
- **Class:** `Customer`
- **Annotations:**
  - `@Entity`
  - `@Table(name = "customer", uniqueConstraints = {@UniqueConstraint(name = "uq_customer_email", columnNames = "email")})`
  - `@EntityListeners(AuditingEntityListener.class)`

### 2.1 Attributes & JPA Mapping

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id", nullable = false, updatable = false)
private Long id;

@Column(name = "email", nullable = false, length = 255, unique = true)
private String email;

@Column(name = "password_hash", nullable = false, length = 60)
private String passwordHash;

@Column(name = "role", nullable = false, length = 20)
private String role;

@Column(name = "enabled", nullable = false)
private boolean enabled;

@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private Instant createdAt;

@LastModifiedDate
@Column(name = "updated_at", nullable = false)
private Instant updatedAt;
```

---

## 3. Entity to API DTO Mapping for Authentication

Per Architecture Decision `AD-4` (DTO / Entity boundary) and Package Map rules:
- Entities are never exposed in Controller methods or API response bodies.
- Authentication operations interact with `Customer` through the Service layer (`AuthService` / `CustomerService`).

```mermaid
classDiagram
    class LoginRequest {
        +String email
        +String password
    }
    class Customer {
        +Long id
        +String email
        +String passwordHash
        +String role
        +boolean enabled
        +Instant createdAt
        +Instant updatedAt
    }
    class LoginResponse {
        +Long id
        +String email
        +String role
    }

    LoginRequest ..> Customer : Input email queried via CustomerRepository; password verified against passwordHash
    Customer ..> LoginResponse : Mapped upon successful verification (excludes credentials and metadata)
```

### 3.1 Inbound Verification: `LoginRequest` vs `Customer`
1. `LoginRequest.email` is normalized (`trim().toLowerCase()`) per `OD-004`.
2. `CustomerRepository.findByEmail(normalizedEmail)` loads the `Customer` entity.
3. `passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())` verifies credentials.
4. `customer.isEnabled()` confirms account active status (`BR-004`).

### 3.2 Outbound Mapping: `Customer` → `LoginResponse`
- `customer.getId()` → `LoginResponse.id`
- `customer.getEmail()` → `LoginResponse.email`
- `customer.getRole()` → `LoginResponse.role`
- *Strictly Excluded:* `passwordHash`, `enabled`, `createdAt`, `updatedAt`

---

## 4. Repository Interface: `CustomerRepository`

- **Package:** `org.example.customerportal.repository`
- **Interface:** `CustomerRepository extends JpaRepository<Customer, Long>`
- **Method for US-002:**
  ```java
  Optional<Customer> findByEmail(String email);
  ```
- **Execution semantics:** Spring Data JPA executes `SELECT c FROM Customer c WHERE c.email = :email` leveraging index `uq_customer_email`.
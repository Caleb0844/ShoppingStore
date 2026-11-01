# Modestoe Architecture

## 1. Development approach

Use **iterative Agile development with vertical feature slices**. Each slice moves through design, implementation, tests, error verification, and documentation. This is preferable to Waterfall because requirements will evolve as marketplace rules become clearer. It is simpler and more focused than a full Spiral process for the current project size.

## 2. Current and target stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA / Hibernate
- MySQL
- Jakarta Bean Validation
- Lombok
- Springdoc OpenAPI
- Spring Security with JWT as authentication is introduced
- Maven

## 3. Layered flow

```text
// Request enters through a controller, is validated, authorized, processed, and persisted.
HTTP client -> Controller -> DTO validation -> Service -> Repository -> MySQL
                                      |
                                      -> Domain rules, transactions, and explicit errors
```

- **Controller:** HTTP routes, request binding, validation entry point, response shape.
- **DTO:** public request/response contracts; do not expose internal persistence structure unnecessarily.
- **Service:** business rules, authorization decisions, transactions, and orchestration.
- **Entity:** database/domain state and relationships.
- **Repository:** persistence queries.
- **Exception handling:** one consistent API error format.

## 4. Package organization

```text
src/main/java/com/caleb/Creator_store/
├── Controllers/       // REST endpoints
├── Services/          // Business use cases
├── Repositories/      // Spring Data persistence
├── Entities/          // JPA entities and relationships
├── Enums/             // Shared status and role enums
├── dto/               // API request and response models
├── security/          // JWT, users, filters, and authorization
└── exceptions/        // Domain exceptions and API error handling
```

Keep the existing package naming consistent unless a deliberate package migration is documented in `memory.md`.

## 5. Domain relationships

```text
// Ownership is explicit; a seller is still a user and may also buy.
User 1 ---- 0..1 SellerProfile
User 1 ---- many Orders
SellerProfile 1 ---- many Products
Order 1 ---- many OrderItems
Product 1 ---- many OrderItems
```

For multi-seller fulfillment, leave room for a future `SellerOrder`/sub-order concept instead of assuming one fulfillment status is enough for every seller in an order.

## 6. API direction

Use resource-oriented REST endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/products`
- `POST /api/seller-applications`
- `POST /api/products` for approved sellers
- `GET /api/me/orders`
- `POST /api/orders`
- `GET /api/admin/...` for restricted administration

The authenticated principal, not a client-supplied user ID, determines ownership.

## 7. Data and security boundaries

- MySQL is the current database.
- Use migrations before production; `ddl-auto=update` is development-only.
- Store passwords as strong hashes, never plaintext.
- Keep KYC fields out of public DTOs and logs.
- Use transactions for checkout, stock changes, and cancellation/stock restoration.
- Use database constraints for uniqueness and required relationships.

## 8. Deployment path

Develop locally first, then deploy to a cloud environment. Keep secrets in environment variables or a secret manager, never in source control.

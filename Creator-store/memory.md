# Modestoe Project Memory

This file records durable decisions, important bugs, migrations, and context for future contributors or AI tools.

## Current state

- Project direction: Modestoe marketplace.
- Current backend: Spring Boot REST API using Java 21, JPA, MySQL, Lombok, validation, and Springdoc.
- Current database configuration uses development-time Hibernate schema updates.
- UI implementation is deferred.
- Development method: iterative Agile feature phases with small verified slices.

## Decisions

- Use one `User` entity with multiple roles: `CUSTOMER`, `SELLER`, `ADMIN`.
- New users begin as `CUSTOMER`.
- Use a separate one-to-one `SellerProfile` for seller details and application/KYC state.
- Seller application statuses are `PENDING`, `APPROVED`, `REJECTED`, and `SUSPENDED`.
- A seller receives seller capability only after approval.
- Products belong to seller profiles.
- One customer order may contain products from multiple sellers.
- Sellers see only order items for their own products.
- Customers see their own complete orders.
- Admins can view and moderate marketplace data.
- Products are public.
- Initial currency is Kenyan shillings (KES), with future flexibility for more currencies.
- Tax is not included initially.
- Shipping is part of the planned order calculation and will later be location/admin controlled.
- M-Pesa is planned later, not in the initial implementation.
- Use Spring Security with JWT for authentication.
- Use REST APIs.
- Develop locally first, then deploy to the cloud.

## Current implementation milestones

- User roles were added and moved into the `Enums` package.
- `SellerProfile` and `SellerApplicationStatus` were added.
- No authentication or admin approval endpoint exists yet.
- A typed exception layer now provides consistent validation, not-found, and business-rule error responses.
- Seller applications now have repository/service support; submission is pending by default and duplicate profiles are rejected.
- Seller application request/response DTOs now define the API boundary; internal user, status-control, KYC, and review-note fields are not exposed in the response.
- Seller application endpoints now submit and retrieve applications through DTOs; the temporary route includes `userId` until JWT authentication is added.
- Focused service tests now cover pending creation, duplicate applications, unknown users, and safe response mapping.
- Seller application review now supports pending-to-approved/rejected and approved-to-suspended transitions; approval grants SELLER and suspension removes it.
- Review transition tests cover approval role assignment, rejection without role assignment, suspension role removal, and invalid transitions.
- Account authentication foundation now includes a password-hash field and a validated registration DTO; registration and JWT wiring are not implemented yet.
- Registration service now normalizes email, rejects duplicates, hashes passwords with BCrypt, assigns CUSTOMER, and returns a safe user response.
- Registration service tests cover BCrypt-hash persistence behavior, CUSTOMER defaults, safe responses, normalization, and duplicate rejection.
- `POST /api/auth/register` now exposes the secure registration flow and returns UserResponse instead of the User entity.
- Login credential verification now normalizes email, checks BCrypt passwords, and returns generic HTTP 401 errors; JWT generation is still pending.
- JWT token generation foundation now uses JJWT, an environment-provided Base64 secret, configurable expiration, and user ID/email/role claims; token parsing and request authentication are still pending.
- JWT parsing now verifies signature/expiration and extracts typed user identity and roles; request filtering and route protection are still pending.
- `POST /api/auth/login` now verifies BCrypt credentials and returns a signed JWT plus safe user data; Spring Security request filtering is still pending.
- Spring Security now uses stateless JWT authentication; auth, public product browsing, Swagger, and API docs are public while other routes require a valid bearer token.
- Seller application self-service now uses the verified JWT principal through `/api/seller-applications/me`; admin review is isolated under an ADMIN-authorized route.
- The legacy entity-based user creation endpoint was removed; user management inspection is now ADMIN-only so registration cannot bypass password hashing.
- Products now belong to seller profiles; only approved sellers can create, update, or delete their own products, while catalog reads remain public through safe DTOs.
- Product service tests now cover approved-seller creation, unapproved-seller rejection, ownership enforcement, field updates, and safe public response mapping.
- Product removal now archives products through `PATCH /api/products/{id}/archive`; archived records remain stored for future order history but are excluded from public catalog reads.
- Orders are now bound to the authenticated customer, use active products and database prices, decrement stock transactionally, and expose only customer-owned DTO responses.
- Persistent carts now belong one-to-one to authenticated customers; only active products can be added, and cart operations return safe DTOs without accepting customer IDs.
- `POST /api/orders/checkout` now converts the authenticated customer's cart into a transactional multi-seller order, revalidates product status/stock/prices, decrements stock, and clears the cart only after saving the order.
- Sellers can now view grouped order summaries containing only their own product lines through `GET /api/orders/seller`; customer personal data and other sellers' lines are excluded.
- Orders now persist separate typed `OrderStatus`, `PaymentStatus`, and `FulfillmentStatus` values; the legacy string status remains temporarily for schema compatibility.
- Status transitions are now enforced in `OrderService`: customers can cancel owned pre-fulfillment orders, sellers can advance fulfillment only for owned order lines, and payment updates allow only valid provider-driven transitions.
- Customer cancellation now restores each order item's stock transactionally; paid orders are rejected until refund handling is implemented.
- Administrators can now inspect complete order data through ADMIN-only `GET /api/admin/orders` and `GET /api/admin/orders/{id}`; customer and seller order boundaries remain unchanged.
- Admin user and seller application views now return safe DTOs only; password hashes, user relationships, KYC identifiers, and review notes remain excluded.
- Audit logging now records non-sensitive metadata for seller moderation, product archiving, order cancellation, fulfillment changes, and payment changes; audit reads are ADMIN-only.

## Known verification limitation

- Maven compile was attempted after the seller-profile move but was blocked by the environment permission prompt. Re-run compilation before relying on the new entity mappings.

## Future spaces

- Authentication/security.
- Seller onboarding and KYC.
- Catalog and product ownership.
- Cart and multi-seller checkout.
- Orders and fulfillment.
- Payments and M-Pesa.
- Addresses and shipping.
- Reviews, ratings, and complaints.
- Notifications.
- Admin moderation and audit.
- Separate customer, seller, and admin clients.

## Maintenance rule

Append new decisions and resolved bugs here. Do not silently rewrite historical decisions; record superseding decisions with the date and reason.

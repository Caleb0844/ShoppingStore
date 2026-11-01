# Modestoe Roadmap and Tasks

## Working method

Use feature phases made of small, independently verifiable vertical slices. Do not begin a later phase until its prerequisites are stable and tested. Each completed task should update relevant documentation and `memory.md`.

## Phase 0: Foundation and safety

- [x] Define marketplace domain rules and roles.
- [x] Create the shared `Enums` package.
- [x] Add the initial seller profile/application model.
- [ ] Confirm entity mappings and compile successfully.
- [x] Add a consistent API error response.
- [ ] Add baseline test conventions.

## Phase 1: Accounts and authentication

- [x] Define registration DTOs and account constraints.
- [x] Implement registration service and password hashing.
- [x] Add registration endpoint with safe response DTO.
- [x] Add password storage using a secure hash.
- [ ] Add Spring Security configuration.
- [x] Add Spring Security configuration and JWT request filter.
- [x] Add login credential verification service and DTOs.
- [x] Add JWT token generation foundation.
- [x] Add JWT token parsing and validation foundation.
- [x] Add JWT login endpoint and token response.
- [ ] Add current-user resolution.
- [x] Add current-user resolution.
- [x] Add admin-only seller-application review route.
- [ ] Add role and ownership authorization tests.

## Phase 2: Seller onboarding

- [x] Create seller application DTOs that exclude internal fields.
- [x] Create seller application repository and service.
- [x] Prevent duplicate or conflicting applications.
- [x] Add seller application service tests.
- [x] Add seller application submission and status endpoints.
- [x] Add admin approve/reject/suspend use cases.
- [x] Add admin review DTO and seller application status transitions.
- [x] Assign `SELLER` only after approval.
- [ ] Protect KYC data and test response serialization.
- [ ] Reserve notification integration for a later slice.

## Phase 3: Catalog and seller products

- [x] Connect products to approved seller profiles.
- [x] Add product create/update DTOs.
- [x] Replace hard deletion with product archiving.
- [x] Allow only approved sellers to create products.
- [x] Enforce seller ownership on update and archive.
- [x] Keep public product browsing available.
- [x] Add product validation and tests.

## Phase 4: Cart and checkout

- [x] Design order ownership around the authenticated customer.
- [x] Validate active product availability and quantities.
- [x] Recalculate authoritative prices at checkout.
- [x] Support one order containing multiple sellers.
- [x] Use a transaction for order creation and stock changes.
- [x] Connect cart checkout to order creation and clear the cart after success.
- [x] Design cart ownership and persistence.
- [ ] Reserve payment fields for future M-Pesa integration.
- [ ] Add KES currency handling and shipping representation.

## Phase 5: Orders and fulfillment

- [x] Connect orders to the authenticated customer.
- [x] Add customer-only complete order views.
- [x] Add seller-filtered order-item views.
- [x] Define order, payment, and fulfillment statuses separately.
- [x] Implement cancellation rules and stock restoration.
- [x] Enforce order, payment, and fulfillment status transitions.
- [ ] Leave location/live tracking for a future phase.

## Phase 6: Administration

- [x] Add protected admin order oversight.
- [x] Add protected admin user and seller views.
- [ ] Add seller application moderation.
- [ ] Add order status oversight.
- [x] Add audit records for important marketplace actions.
- [ ] Reserve review, rating, and complaint moderation endpoints.

## Phase 7: Marketplace trust and growth

- [ ] Add reviews and ratings tied to completed purchases.
- [ ] Add complaints and moderation workflow.
- [ ] Add seller notifications.
- [ ] Add delivery addresses and location-based shipping fees.
- [ ] Integrate M-Pesa.
- [ ] Add seller analytics, payouts, and reporting.

## Definition of done for every task

1. The behavior is implemented in the correct layer.
2. External input is validated.
3. Authorization and ownership are tested.
4. Errors are explicit and consistent.
5. Relevant tests and compilation pass.
6. Documentation and `memory.md` are updated.

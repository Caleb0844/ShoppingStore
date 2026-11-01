# Modestoe Product Requirements Document

## 1. Product summary

Modestoe is a Kenyan-shilling marketplace where customers discover products, add them to a cart, and place orders from one or more approved sellers. A user can be a customer and can later apply to become a seller. Administrators supervise marketplace safety and operations.

The first implementation is a REST backend. UI work is intentionally deferred.

## 2. Problem

Creators and customers need one trusted place to sell and buy products. The platform must protect seller ownership, customer order privacy, stock, payments, and sensitive seller application information while remaining flexible for future marketplace features.

## 3. Target users

- **Buyer/customer:** browses products, manages a cart, checks out, and views their own orders.
- **Seller:** an approved user who manages their own products and sees only sales involving those products.
- **Administrator:** reviews seller applications and moderates marketplace activity.

## 4. MVP scope

The initial MVP includes:

1. User accounts and JWT-based authentication.
2. Public product catalog.
3. Cart and checkout.
4. Customer order history.
5. Multi-seller orders.
6. Seller application and approval foundation.
7. Role-based and ownership-based authorization.
8. MySQL persistence and REST endpoints.

The current code already contains early user, product, order, order-item, and seller-profile foundations. New work must preserve valid existing behavior or explicitly document a migration.

## 5. Core domain rules

- Every new user starts as a `CUSTOMER`.
- A user may hold multiple roles: `CUSTOMER`, `SELLER`, and `ADMIN`.
- A seller application begins as `PENDING`.
- Only an approved seller may publish or manage products.
- Products belong to a seller profile.
- Sellers may manage only their own products.
- Products are publicly viewable.
- Any authenticated user may buy, including sellers.
- An order belongs to the authenticated customer.
- One customer order may contain products from multiple sellers.
- Sellers see only order items belonging to them, with limited customer information.
- Customers see their own complete orders.
- Administrators can view and moderate marketplace data.
- Prices are represented using `BigDecimal`; the initial currency is Kenyan shillings.
- Tax is not included initially; shipping is included according to the later shipping rules.
- A cart may reflect a current product price at checkout; the server is authoritative for final pricing.

## 6. Future feature spaces

- Seller KYC document workflow and notifications.
- M-Pesa payments and refunds.
- Addresses, delivery zones, and admin-managed shipping fees.
- Seller fulfillment and delivery tracking.
- Reviews, ratings, comments, and complaints.
- Admin moderation and audit history.
- Seller analytics and payouts.
- Search, categories, images, and recommendations.
- Customer and seller web/mobile applications.

## 7. Success criteria

- Users can authenticate securely.
- Unauthorized ownership and role actions are rejected.
- A valid customer can complete a multi-seller checkout.
- Stock and order writes are transactionally consistent.
- Sensitive KYC data is not exposed through public APIs.
- Each feature has automated tests and documented decisions.

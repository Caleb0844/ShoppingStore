# Modestoe Engineering Rules

## 1. General development rules

- Make one focused change at a time.
- Read related code before editing.
- Preserve existing behavior unless the task intentionally changes it.
- Prefer reusable services and helpers over duplicated logic.
- Keep code understandable, type-safe, secure, and performant.
- Do not add a library when an existing project dependency or standard Spring feature is sufficient.
- Do not create speculative microservices or abstractions.

## 2. Comments and code blocks

When adding a non-trivial code block, include a concise comment explaining:

1. What the code does.
2. Where it can be reused.
3. Whether it is safe to change and what behavior could be affected.

Comments must explain intent, not obvious syntax. For JSON, SQL, or configuration examples, place the explanation immediately before the block if inline comments would make the example invalid. Before changing existing code, compare its current behavior and comments with the proposed implementation. If a change would violate an important existing rule, find an alternative or document the decision.

## 3. Validation and errors

- Validate all external input at API boundaries.
- Use DTOs for public requests where domain entities would expose internal fields.
- Return explicit, consistent error responses.
- Do not catch broad exceptions silently.
- Do not return success-shaped fallback data after a failure.
- Use domain-specific exceptions and centralized exception handling.

## 4. Authorization

- Authentication identifies the current user.
- Authorization checks both role and ownership.
- Never trust client-supplied `userId`, `sellerId`, price, total, or role.
- Sellers can manage only their own products.
- Customers can view only their own complete orders.
- Sellers can view only their relevant order items.
- Admin actions must be explicitly protected and auditable.

## 5. Security and privacy

- Hash passwords using an approved password encoder.
- Protect JWT secrets and database credentials outside source control.
- Do not expose passwords, national IDs, KYC documents, review notes, or internal audit data publicly.
- Avoid logging tokens, credentials, payment data, or KYC data.
- Apply least privilege to roles and endpoints.
- Plan retention and deletion rules for KYC information.

## 6. Data and money

- Use `BigDecimal` for money.
- Store the currency explicitly when the payment model is introduced; default to KES initially.
- The server calculates totals and validates stock.
- Preserve `priceAtPurchase` for historical orders.
- Use transactions for checkout, stock updates, and cancellations.
- Do not hard-delete historical products referenced by orders; prefer archival/status changes.

## 7. Testing

- Add unit tests for domain rules and service behavior.
- Add controller tests for validation and authorization boundaries.
- Add repository/integration tests for relationships and transactions.
- Test both allowed and denied paths.
- After each feature slice, run the smallest relevant tests, then compile.
- Record failures and their resolution in `memory.md`.

## 8. Documentation maintenance

Update `PRD.md` when scope changes, `architecture.md` when structure changes, `task.md` when work moves, and `memory.md` when decisions, bugs, or migrations occur.

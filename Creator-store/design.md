# Modestoe Design Direction

## 1. Scope

UI implementation is intentionally deferred. This file records backend-facing design decisions and reserves the visual system so future customer, seller, and admin applications remain consistent.

## 2. Product experience principles

- Trustworthy: make ownership, order state, and moderation clear.
- Understandable: use plain language and predictable actions.
- Secure by default: never expose private seller or customer information unnecessarily.
- Consistent: use the same names for roles, statuses, and resources across APIs and future clients.
- Responsive: design future interfaces for mobile-first marketplace use.

## 3. Reserved application areas

- Customer storefront and catalog.
- Cart and checkout.
- Customer order history.
- Public seller profile.
- Seller product workspace.
- Seller sales/order workspace.
- Admin moderation workspace.
- Authentication and account settings.

## 4. Visual system

The following are intentionally `TBD` until UI discovery begins:

- Primary color: TBD.
- Secondary color: TBD.
- Accent and status colors: TBD.
- Font family: TBD.
- Heading and body typography scale: TBD.
- Component library: TBD.
- Spacing scale: TBD.
- Border radius, elevation, icons, and imagery: TBD.

When UI work starts, these values must be selected once, documented here, and reused rather than invented per screen.

## 5. Status presentation

Future clients should use consistent labels and visual meanings for:

- Seller application: `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`.
- Product availability: future status model to be agreed before implementation.
- Order, payment, and fulfillment: separate status concepts, not one overloaded label.

## 6. Accessibility and privacy

- Use clear labels and actionable error messages.
- Do not display KYC or private contact data in public seller views.
- Do not rely on color alone to communicate status.
- Preserve keyboard, screen-reader, and responsive behavior when UI work begins.

package com.caleb.Creator_store.Enums;

// Defines auditable marketplace actions without storing sensitive request payloads.
public enum AuditAction {
    SELLER_APPLICATION_APPROVED,
    SELLER_APPLICATION_REJECTED,
    SELLER_APPLICATION_SUSPENDED,
    PRODUCT_ARCHIVED,
    ORDER_CANCELLED,
    FULFILLMENT_STATUS_CHANGED,
    PAYMENT_STATUS_CHANGED
}

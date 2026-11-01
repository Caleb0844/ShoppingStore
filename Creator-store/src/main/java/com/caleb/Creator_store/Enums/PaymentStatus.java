package com.caleb.Creator_store.Enums;

// Describes payment state so future M-Pesa processing does not alter order lifecycle state.
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

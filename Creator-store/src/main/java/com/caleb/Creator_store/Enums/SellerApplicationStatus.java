package com.caleb.Creator_store.Enums;

// Defines the lifecycle states of a seller application and seller access.
// Status transitions belong in the service workflow, not in controllers or clients.
public enum SellerApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

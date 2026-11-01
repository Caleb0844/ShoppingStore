package com.caleb.Creator_store.Enums;

// Describes delivery progress independently from order and payment decisions.
public enum FulfillmentStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

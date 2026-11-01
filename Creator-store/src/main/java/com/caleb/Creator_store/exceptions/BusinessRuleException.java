package com.caleb.Creator_store.exceptions;

// Represents a valid request that violates a Modestoe business rule.
// Reuse this exception in services for expected domain failures; changing it affects API error handling.
public class BusinessRuleException extends RuntimeException {

    // Preserves the business explanation so GlobalExceptionHandler can return it to the API client.
    public BusinessRuleException(String message) {
        super(message);
    }
}

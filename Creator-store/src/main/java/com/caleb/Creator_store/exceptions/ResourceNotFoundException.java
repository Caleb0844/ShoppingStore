package com.caleb.Creator_store.exceptions;

// Represents a requested Modestoe resource that does not exist.
// Reuse it from services instead of raw RuntimeException so clients receive HTTP 404 responses.
public class ResourceNotFoundException extends RuntimeException {

    // Preserves the resource-specific message for the centralized API error response.
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

package com.caleb.Creator_store.exceptions;

// Represents a missing, expired, malformed, or tampered JWT presented to the API.
// Reuse this exception at the authentication boundary so invalid tokens never authorize a request.
public class InvalidTokenException extends RuntimeException {

    // Keeps token failures generic so token structure and signing details are not disclosed.
    public InvalidTokenException() {
        super("Invalid or expired authentication token");
    }

    // Preserves the cause for server-side diagnostics without exposing it to API clients.
    public InvalidTokenException(Throwable cause) {
        super("Invalid or expired authentication token", cause);
    }
}

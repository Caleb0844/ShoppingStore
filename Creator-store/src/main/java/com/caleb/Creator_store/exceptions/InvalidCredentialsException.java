package com.caleb.Creator_store.exceptions;

// Represents a failed login without revealing whether the email or password was incorrect.
// Reuse this exception for credential failures so authentication responses do not leak account details.
public class InvalidCredentialsException extends RuntimeException {

    // Uses one generic message for all invalid credential combinations.
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}

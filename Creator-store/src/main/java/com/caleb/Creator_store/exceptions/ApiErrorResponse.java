package com.caleb.Creator_store.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

// Defines the stable JSON shape returned when an expected API error occurs.
// Reuse this response for future validation, authorization, and domain error handlers.
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors) {
}

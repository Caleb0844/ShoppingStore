package com.caleb.Creator_store.exceptions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Converts expected application failures into consistent REST responses.
// Extend this class for new known exceptions; unexpected failures must not be silently disguised.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Converts Bean Validation failures into field-level messages clients can display.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {
        // Converts field validation failures into a stable response clients can reuse.
        Map<String, String> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null
                                ? "Invalid value"
                                : error.getDefaultMessage(),
                        (firstMessage, secondMessage) -> firstMessage));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more fields are invalid", fieldErrors);
    }

    // Converts missing resources into HTTP 404 responses.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found",
                exception.getMessage(), Collections.emptyMap());
    }

    // Converts business-rule violations into HTTP 400 responses.
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(
            BusinessRuleException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Business rule violation",
                exception.getMessage(), Collections.emptyMap());
    }

    // Converts failed authentication into HTTP 401 without exposing which credential was incorrect.
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed",
                exception.getMessage(), Collections.emptyMap());
    }

    // Converts invalid or expired tokens into HTTP 401 without exposing token-validation details.
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTokenException(
            InvalidTokenException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed",
                exception.getMessage(), Collections.emptyMap());
    }

    // Centralizes response construction so every handled error has the same JSON structure.
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            Map<String, String> fieldErrors) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                fieldErrors);

        return ResponseEntity.status(status).body(response);
    }
}

package com.reliaquest.api.exception;

import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<Object> handleRetryableFeignException(CallNotPermittedException ex) {
        // This occurs when circuit breaker state is OPEN, an alert can be raised from here based on requirements
        log.error("CallNotPermittedException occurred: {}", ex.getMessage());
        return buildResponseStructure(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Temporary issue. Please retry.",
                "Downstream service is currently unreachable");
    }

    // Handle all unhandled exceptions

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Object> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "No data found", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<Object> handleRetryableException(RetryableException ex) {
        log.error("RetryableException occurred: {}", ex.getMessage());
        return buildResponseStructure(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "Error while executing request");
    }

    @ExceptionHandler(RetryableFeignException.class)
    public ResponseEntity<Object> handleRetryableFeignException(RetryableFeignException ex) {
        log.error("RetryableFeignException occurred: {}", ex.getMessage());
        return buildResponseStructure(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "Error while executing request");
    }

    @ExceptionHandler(NonRetryableFeignException.class)
    public ResponseEntity<Object> handleNonRetryableFeignException(NonRetryableFeignException ex) {
        log.error("NonRetryableFeignException occurred: {}", ex.getMessage());
        if (ex.getStatus() == HttpStatus.NOT_FOUND.value()) {
            return buildResponseStructure(
                    HttpStatus.valueOf(ex.getStatus()), "No data found", "No data found for the given request");
        }
        return buildResponseStructure(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Request could not be processed",
                "Unable to process the request due to client error");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid parameter type", ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Validation failed");
        body.put("details", "One or more fields are invalid.");

        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()))
                .toList();

        body.put("fieldErrors", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus httpStatus, String invalidParameterType, Exception ex) {
        return buildResponseStructure(httpStatus, invalidParameterType, ex.getMessage());
    }

    // Helper method to build a standard response
    private ResponseEntity<Object> buildResponseStructure(HttpStatus status, String message, String details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("details", details);
        return new ResponseEntity<>(body, status);
    }
}

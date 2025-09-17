package com.reliaquest.api.service.helper;

import com.reliaquest.api.exception.NonRetryableFeignException;
import com.reliaquest.api.exception.RetryableFeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.util.Set;
import org.springframework.http.HttpStatus;

public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    // List of status codes that should NOT be retried
    private static final Set<Integer> NON_RETRYABLE_STATUS_CODES = Set.of(
            HttpStatus.BAD_REQUEST.value(), // 400
            HttpStatus.UNAUTHORIZED.value(), // 401
            HttpStatus.FORBIDDEN.value(), // 403
            HttpStatus.NOT_FOUND.value() // 404
            );

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        // If the status is non-retryable, return a specific exception (not RuntimeException)
        if (NON_RETRYABLE_STATUS_CODES.contains(status)) {
            return new NonRetryableFeignException("Non-retryable error: " + status + " from " + methodKey, status);
        }

        // For other status codes, throw RetryableFeignException so Resilience4j will retry
        return new RetryableFeignException("Retryable error: " + status + " from " + methodKey, status);
    }
}

package com.reliaquest.api.config;

import io.github.resilience4j.retry.RetryRegistry;

// @Component
public class RetryEventLogger {

    public RetryEventLogger(RetryRegistry retryRegistry) {
        retryRegistry.getAllRetries().forEach(retry -> retry.getEventPublisher()
                .onRetry(event -> System.out.println("/////Retry attempt #" + event.getNumberOfRetryAttempts() + " for "
                        + event.getName() + " due to: "
                        + event.getLastThrowable())));
    }
}

package com.reliaquest.api.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    private static final Logger logger = LoggerFactory.getLogger(ResilienceConfig.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public ResilienceConfig(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void registerListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            cb.getEventPublisher()
                    .onStateTransition(event -> logger.info(
                            "CircuitBreaker '{}' state changed to {}", cb.getName(), event.getStateTransition()))
                    .onError(event -> logger.warn(
                            "CircuitBreaker '{}' error: {}",
                            cb.getName(),
                            event.getThrowable().toString()));
        });

        retryRegistry.getAllRetries().forEach(retry -> {
            retry.getEventPublisher()
                    .onRetry(event -> logger.info(
                            "Retry '{}' performed due to {}, attempt {}",
                            retry.getName(),
                            event.getLastThrowable(),
                            event.getNumberOfRetryAttempts()))
                    .onError(event -> logger.warn(
                            "Retry '{}' error: {}",
                            retry.getName(),
                            event.getLastThrowable().toString()));
        });
    }
}

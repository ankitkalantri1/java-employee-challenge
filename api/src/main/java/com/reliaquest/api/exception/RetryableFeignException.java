package com.reliaquest.api.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetryableFeignException extends RuntimeException {
    private int status;

    public RetryableFeignException(String message, int status) {
        super(message);
        this.status = status;
    }
}

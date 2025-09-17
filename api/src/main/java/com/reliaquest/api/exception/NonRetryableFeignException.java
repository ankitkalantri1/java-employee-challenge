package com.reliaquest.api.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonRetryableFeignException extends RuntimeException {
    private int status;

    public NonRetryableFeignException(String message, int status) {
        super(message);
        this.status = status;
    }
}

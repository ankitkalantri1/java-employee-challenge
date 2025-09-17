package com.reliaquest.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private T data;
    private String status;
}

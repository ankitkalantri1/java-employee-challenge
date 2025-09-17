package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeRequest {

    @NotBlank
    private String name;

    @Min(1)
    private int salary;

    @Min(16)
    @Max(75)
    private int age;

    @NotBlank
    private String title;
}

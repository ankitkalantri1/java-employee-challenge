package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeDto {
    private String id;

    @JsonProperty("employee_name")
    private String name;

    @JsonProperty("employee_salary")
    private int salary;

    @JsonProperty("employee_age")
    private int age;

    @JsonProperty("employee_title")
    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("employee_email")
    private String email;
    // getters and setters
}

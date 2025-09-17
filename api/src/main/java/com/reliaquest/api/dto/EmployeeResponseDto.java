package com.reliaquest.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponseDto {
    private String id;
    private String name;
    private int salary;
    private int age;
    private String title;
    private String email;
}

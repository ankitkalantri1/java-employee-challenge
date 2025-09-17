package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeResponseDto;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.OptionalInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeController implements IEmployeeController<EmployeeResponseDto, EmployeeRequest> {

    @Autowired
    private EmployeeService employeeService;

    @Override
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees() {
        List<EmployeeResponseDto> employees;
        employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByNameSearch(@PathVariable String searchString) {
        List<EmployeeResponseDto> employees = employeeService.searchEmployeesByName(searchString);
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable String id) {
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return employee != null
                ? ResponseEntity.ok(employee)
                : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        OptionalInt highestSalary = employeeService.getHighestSalary();
        if (highestSalary.isPresent()) {
            return ResponseEntity.ok(highestSalary.getAsInt());
        } else {
            throw new EmployeeNotFoundException("No employees found for calculating highest salary");
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> names = employeeService.getTopTenHighestEarningEmployeeNames();
        return ResponseEntity.ok(names);
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> createEmployee(@RequestBody @Valid EmployeeRequest employeeInput) {
        EmployeeResponseDto created = employeeService.createEmployee(employeeInput);
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        boolean deleted = employeeService.deleteEmployeeById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new EmployeeNotFoundException("No employee found with id: " + id);
        }
    }
}

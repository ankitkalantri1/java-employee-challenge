package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeResponseDto;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.EmployeeRequest;
import java.text.MessageFormat;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
/**
 * Service to handle business logic related to employees.
 * Interacts with EmployeeDataService for data retrieval and manipulation.
 */
public class EmployeeService {

    @Autowired
    private EmployeeDataService employeeDataService;

    public List<EmployeeResponseDto> searchEmployeesByName(String searchString) {
        List<EmployeeResponseDto> employees = employeeDataService.getAllEmployees();
        return employees.stream()
                .filter(emp -> emp.getName() != null
                        && (emp.getName().equalsIgnoreCase(searchString)
                                || emp.getName().toLowerCase().contains(searchString.toLowerCase())))
                .collect(Collectors.toList());
    }

    public OptionalInt getHighestSalary() {
        List<EmployeeResponseDto> employees = employeeDataService.getAllEmployees();
        return employees.stream().mapToInt(EmployeeResponseDto::getSalary).max();
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<EmployeeResponseDto> employees = employeeDataService.getAllEmployees();
        return employees.stream()
                .sorted((e1, e2) -> Integer.compare(e2.getSalary(), e1.getSalary()))
                .limit(10)
                .map(EmployeeResponseDto::getName)
                .collect(Collectors.toList());
    }

    public boolean deleteEmployeeById(String id) {
        List<EmployeeResponseDto> employees = employeeDataService.getAllEmployees();
        String name = employees.stream()
                .filter(emp -> emp.getId().equals(id))
                .map(EmployeeResponseDto::getName)
                .findFirst()
                .orElseThrow(() ->
                        new EmployeeNotFoundException(MessageFormat.format("Employee with id {0} not found", id)));

        EmployeeRequest request = new EmployeeRequest();
        request.setName(name);

        return employeeDataService.deleteEmployeeByName(request, id);
    }

    public EmployeeResponseDto getEmployeeById(String id) {
        return employeeDataService.getEmployeeById(id);
    }

    public EmployeeResponseDto createEmployee(EmployeeRequest employeeRequest) {
        return employeeDataService.createEmployee(employeeRequest);
    }

    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeDataService.getAllEmployees();
    }
}

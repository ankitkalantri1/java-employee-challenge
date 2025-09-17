package com.reliaquest.api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.constants.AppConstants;
import com.reliaquest.api.dto.EmployeeDto;
import com.reliaquest.api.dto.EmployeeResponseDto;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
/**
 * Service to interact with EmployeeClient and manage employee data with caching.
 * Uses Caffeine for caching and Resilience4j for fault tolerance.
 */
public class EmployeeDataService {

    @Autowired
    private EmployeeClient employeeClient;

    // Single cache instance storing Map<String (employeeId), EmployeeDto>
    private final Cache<String, Map<String, EmployeeResponseDto>> cache = Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(10, TimeUnit.MINUTES) // Only store one entry with key "all"
            .build();

    @Retry(name = "employeeService")
    @CircuitBreaker(name = "employeeService")
    public List<EmployeeResponseDto> getAllEmployees() {
        Map<String, EmployeeResponseDto> cachedMap = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
        if (cachedMap != null) {
            log.info("Returning employees from cache");
            return cachedMap.values().stream().toList();
        }

        log.info("Fetching employees from Employee Service");
        ApiResponse<List<EmployeeDto>> response = employeeClient.getAllEmployees();
        List<EmployeeDto> employees = response.getData();

        if (employees == null) {
            employees = new ArrayList<>();
        }
        Map<String, EmployeeResponseDto> employeeMap = employees.stream()
                .filter(e -> e.getId() != null)
                .map(this::toResponse)
                .collect(Collectors.toConcurrentMap(EmployeeResponseDto::getId, e -> e));

        cache.put(AppConstants.CACHE_KEY_ALL, employeeMap); // populate the cache
        return new ArrayList<>(employeeMap.values());
    }

    @Retry(name = "employeeService")
    @CircuitBreaker(name = "employeeService")
    public EmployeeResponseDto getEmployeeById(String id) {
        Map<String, EmployeeResponseDto> employeeMap = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);

        if (employeeMap != null && employeeMap.containsKey(id)) {
            log.info("Returning employee from cache for ID: {}", id);
            return employeeMap.get(id);
        }

        log.info("Fetching employee by ID from Employee Service: {}", id);
        ApiResponse<EmployeeDto> response = employeeClient.getEmployeeById(id);
        EmployeeDto employee = response.getData();
        EmployeeResponseDto employeeResponseDto = null;
        if (employee != null) {
            employeeResponseDto = toResponse(employee);
            if (employeeMap == null) {
                employeeMap = new ConcurrentHashMap<>();
            }
            employeeMap.put(id, employeeResponseDto);
            cache.put(AppConstants.CACHE_KEY_ALL, employeeMap);
        }
        return employeeResponseDto;
    }

    @Retry(name = "employeeService")
    @CircuitBreaker(name = "employeeService")
    public EmployeeResponseDto createEmployee(EmployeeRequest employeeInput) {
        log.info("Creating employee");
        ApiResponse<EmployeeDto> response = employeeClient.createEmployee(employeeInput);
        EmployeeDto createdEmployee = response.getData();
        EmployeeResponseDto employeeResponseDto = null;
        if (createdEmployee != null) {
            employeeResponseDto = toResponse(createdEmployee);
            Map<String, EmployeeResponseDto> employeeMap = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
            if (employeeMap == null) {
                employeeMap = new ConcurrentHashMap<>();
            }
            employeeMap.put(createdEmployee.getId(), employeeResponseDto);
            cache.put(AppConstants.CACHE_KEY_ALL, employeeMap);
        }

        return employeeResponseDto;
    }

    @Retry(name = "employeeService")
    @CircuitBreaker(name = "employeeService")
    public boolean deleteEmployeeByName(EmployeeRequest employeeRequest, String id) {
        log.info("Deleting employee by name: {}", employeeRequest.getName());
        ApiResponse<Boolean> response = employeeClient.deleteEmployeeByName(employeeRequest);
        boolean deleted = response.getData() != null && response.getData();
        if (deleted) {
            Map<String, EmployeeResponseDto> employeeMap = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
            if (employeeMap != null) {
                // Remove employees with matching name and update cache
                employeeMap.values().removeIf(emp -> id.equals(emp.getId()));
                cache.put(AppConstants.CACHE_KEY_ALL, employeeMap);
            }
        }
        return deleted;
    }

    EmployeeResponseDto toResponse(EmployeeDto dto) {
        EmployeeResponseDto response = new EmployeeResponseDto();
        response.setId(dto.getId());
        response.setName(dto.getName());
        response.setSalary(dto.getSalary());
        response.setAge(dto.getAge());
        response.setTitle(dto.getTitle());
        response.setEmail(dto.getEmail());
        return response;
    }
}

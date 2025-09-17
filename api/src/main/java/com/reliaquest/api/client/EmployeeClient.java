package com.reliaquest.api.client;

import com.reliaquest.api.config.FeignConfig;
import com.reliaquest.api.dto.EmployeeDto;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeRequest;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "employeeClient", url = "${employee.service.baseUrl}", configuration = FeignConfig.class)
public interface EmployeeClient {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<EmployeeDto>> getAllEmployees();

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<EmployeeDto> getEmployeeById(@PathVariable String id);

    @PostMapping
    ApiResponse<EmployeeDto> createEmployee(@RequestBody EmployeeRequest employee);

    @DeleteMapping()
    ApiResponse<Boolean> deleteEmployeeByName(@RequestBody EmployeeRequest employee);
}

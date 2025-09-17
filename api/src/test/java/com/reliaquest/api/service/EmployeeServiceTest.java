package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.EmployeeResponseDto;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.EmployeeRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeDataService employeeDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchEmployeesByName_MatchExactAndContains() {
        EmployeeResponseDto emp1 = new EmployeeResponseDto();
        emp1.setId("1");
        emp1.setName("Alice");

        EmployeeResponseDto emp2 = new EmployeeResponseDto();
        emp2.setId("2");
        emp2.setName("Al");

        EmployeeResponseDto emp3 = new EmployeeResponseDto();
        emp3.setId("3");
        emp3.setName(null);

        List<EmployeeResponseDto> employees = List.of(emp1, emp2, emp3);

        when(employeeDataService.getAllEmployees()).thenReturn(employees);

        List<EmployeeResponseDto> result = employeeService.searchEmployeesByName("Ali");

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());

        result = employeeService.searchEmployeesByName("alice");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());

        result = employeeService.searchEmployeesByName("Bob");
        assertTrue(result.isEmpty());

        verify(employeeDataService, times(3)).getAllEmployees();
    }

    @Test
    void testGetHighestSalary_ReturnsMax() {
        EmployeeResponseDto emp1 = new EmployeeResponseDto();
        emp1.setSalary(1000);

        EmployeeResponseDto emp2 = new EmployeeResponseDto();
        emp2.setSalary(2000);

        EmployeeResponseDto emp3 = new EmployeeResponseDto();
        emp3.setSalary(1500);

        when(employeeDataService.getAllEmployees()).thenReturn(List.of(emp1, emp2, emp3));

        OptionalInt maxSalary = employeeService.getHighestSalary();

        assertTrue(maxSalary.isPresent());
        assertEquals(2000, maxSalary.getAsInt());

        when(employeeDataService.getAllEmployees()).thenReturn(Collections.emptyList());
        maxSalary = employeeService.getHighestSalary();
        assertTrue(maxSalary.isEmpty());

        verify(employeeDataService, times(2)).getAllEmployees();
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        List<EmployeeResponseDto> employees = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            EmployeeResponseDto emp = new EmployeeResponseDto();
            emp.setSalary(i * 100);
            emp.setName("Emp" + i);
            employees.add(emp);
        }

        when(employeeDataService.getAllEmployees()).thenReturn(employees);

        List<String> topTen = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(10, topTen.size());
        assertEquals("Emp15", topTen.get(0));
        assertEquals("Emp6", topTen.get(9));

        // Below code is to check when number of employees are less than 10
        when(employeeDataService.getAllEmployees()).thenReturn(employees.subList(0, 5));
        topTen = employeeService.getTopTenHighestEarningEmployeeNames();
        assertEquals(5, topTen.size());

        verify(employeeDataService, times(2)).getAllEmployees();
    }

    @Test
    void testDeleteEmployeeById_Success() {
        EmployeeResponseDto emp1 = new EmployeeResponseDto();
        emp1.setId("1");
        emp1.setName("Alice");

        EmployeeResponseDto emp2 = new EmployeeResponseDto();
        emp2.setId("2");
        emp2.setName("Bob");

        when(employeeDataService.getAllEmployees()).thenReturn(List.of(emp1, emp2));

        EmployeeRequest expectedRequest = new EmployeeRequest();
        expectedRequest.setName("Alice");

        when(employeeDataService.deleteEmployeeByName(expectedRequest, "1")).thenReturn(true);

        boolean result = employeeService.deleteEmployeeById("1");

        assertTrue(result);
        verify(employeeDataService).getAllEmployees();
        verify(employeeDataService).deleteEmployeeByName(expectedRequest, "1");
    }

    @Test
    void testDeleteEmployeeById_ThrowsExceptionWhenNotFound() {
        when(employeeDataService.getAllEmployees()).thenReturn(Collections.emptyList());

        EmployeeNotFoundException ex =
                assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployeeById("nonexistent"));

        assertEquals("Employee with id nonexistent not found", ex.getMessage());

        verify(employeeDataService).getAllEmployees();
        verify(employeeDataService, never()).deleteEmployeeByName(any(), any());
    }

    @Test
    void testGetEmployeeById_Delegates() {
        EmployeeResponseDto emp = new EmployeeResponseDto();
        emp.setId("123");
        when(employeeDataService.getEmployeeById("123")).thenReturn(emp);

        EmployeeResponseDto result = employeeService.getEmployeeById("123");

        assertEquals(emp, result);
        verify(employeeDataService).getEmployeeById("123");
    }

    @Test
    void testCreateEmployee_Delegates() {
        EmployeeRequest req = new EmployeeRequest();
        req.setName("NewEmp");

        EmployeeResponseDto emp = new EmployeeResponseDto();
        emp.setId("abc");
        emp.setName("NewEmp");

        when(employeeDataService.createEmployee(req)).thenReturn(emp);

        EmployeeResponseDto result = employeeService.createEmployee(req);

        assertEquals(emp, result);
        verify(employeeDataService).createEmployee(req);
    }

    @Test
    void testGetAllEmployees_Delegates() {
        List<EmployeeResponseDto> employees = List.of(new EmployeeResponseDto());
        when(employeeDataService.getAllEmployees()).thenReturn(employees);

        List<EmployeeResponseDto> result = employeeService.getAllEmployees();

        assertEquals(employees, result);
        verify(employeeDataService).getAllEmployees();
    }
}

package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.benmanes.caffeine.cache.Cache;
import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.constants.AppConstants;
import com.reliaquest.api.dto.EmployeeDto;
import com.reliaquest.api.dto.EmployeeResponseDto;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class EmployeeDataServiceTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmployeeDataServiceTest.class);

    @InjectMocks
    EmployeeDataService employeeDataService;

    @Mock
    EmployeeClient employeeClient;

    Cache<String, Map<String, EmployeeResponseDto>> cache;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        cache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .maximumSize(1)
                .build();
        try {
            var field = EmployeeDataService.class.getDeclaredField("cache");
            field.setAccessible(true);
            field.set(employeeDataService, cache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetAllEmployees_ReturnsFromCache() {
        Map<String, EmployeeResponseDto> cacheMap = new ConcurrentHashMap<>();
        EmployeeResponseDto emp = new EmployeeResponseDto();
        emp.setId("1");
        emp.setName("John");
        cacheMap.put("1", emp);
        cache.put(AppConstants.CACHE_KEY_ALL, cacheMap);

        List<EmployeeResponseDto> result = employeeDataService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
        verifyNoInteractions(employeeClient); // client not called since cache hit
    }

    @Test
    void testGetAllEmployees_FetchesFromClientAndCaches() {
        EmployeeDto empDto1 = new EmployeeDto();
        empDto1.setId("1");
        empDto1.setName("Alice");

        EmployeeDto empDto2 = new EmployeeDto();
        empDto2.setId("2");
        empDto2.setName("Bob");

        ApiResponse<List<EmployeeDto>> apiResponse = new ApiResponse<>();
        apiResponse.setData(List.of(empDto1, empDto2));

        when(employeeClient.getAllEmployees()).thenReturn(apiResponse);

        List<EmployeeResponseDto> result = employeeDataService.getAllEmployees();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Alice")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Bob")));

        Map<String, EmployeeResponseDto> cached = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
        assertNotNull(cached);
        assertEquals(2, cached.size());

        List<EmployeeResponseDto> cachedEmployees = employeeDataService.getAllEmployees();
        verify(employeeClient, times(1)).getAllEmployees();
    }

    @Test
    void testGetAllEmployees_NullDataFromClient() {
        ApiResponse<List<EmployeeDto>> apiResponse = new ApiResponse<>();
        apiResponse.setData(null);

        when(employeeClient.getAllEmployees()).thenReturn(apiResponse);

        List<EmployeeResponseDto> result = employeeDataService.getAllEmployees();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeClient, times(1)).getAllEmployees();

        Map<String, EmployeeResponseDto> cached = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
        assertNotNull(cached);
        assertTrue(cached.isEmpty());
    }

    @Test
    void testGetEmployeeById_ReturnsFromCache() {
        EmployeeResponseDto cachedEmp = new EmployeeResponseDto();
        cachedEmp.setId("123");
        cachedEmp.setName("CachedName");
        Map<String, EmployeeResponseDto> map = new ConcurrentHashMap<>();
        map.put("123", cachedEmp);
        cache.put(AppConstants.CACHE_KEY_ALL, map);

        EmployeeResponseDto result = employeeDataService.getEmployeeById("123");

        assertNotNull(result);
        assertEquals("CachedName", result.getName());
        verifyNoInteractions(employeeClient);
    }

    @Test
    void testGetEmployeeById_FetchesFromClientAndCaches() {
        EmployeeDto empDto = new EmployeeDto();
        empDto.setId("999");
        empDto.setName("FetchedName");

        ApiResponse<EmployeeDto> apiResponse = new ApiResponse<>();
        apiResponse.setData(empDto);

        when(employeeClient.getEmployeeById("999")).thenReturn(apiResponse);

        EmployeeResponseDto result = employeeDataService.getEmployeeById("999");

        assertNotNull(result);
        assertEquals("FetchedName", result.getName());

        EmployeeResponseDto cachedEmployeeData = employeeDataService.getEmployeeById("999");
        verify(employeeClient, times(1)).getEmployeeById("999");
        Map<String, EmployeeResponseDto> cached = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
        assertNotNull(cached);
        assertTrue(cached.containsKey("999"));
    }

    @Test
    void testCreateEmployee_AddsToCache() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("NewEmp");

        EmployeeDto empDto = new EmployeeDto();
        empDto.setId("777");
        empDto.setName("NewEmp");

        ApiResponse<EmployeeDto> apiResponse = new ApiResponse<>();
        apiResponse.setData(empDto);

        when(employeeClient.createEmployee(request)).thenReturn(apiResponse);

        EmployeeResponseDto created = employeeDataService.createEmployee(request);

        assertNotNull(created);
        assertEquals("NewEmp", created.getName());

        Map<String, EmployeeResponseDto> cached = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
        assertNotNull(cached);
        assertTrue(cached.containsKey("777"));

        verify(employeeClient, times(1)).createEmployee(request);
    }

    @Test
    void testDeleteEmployeeByName_DeletesFromCache() {
        EmployeeResponseDto emp1 = new EmployeeResponseDto();
        emp1.setId("id1");
        emp1.setName("Name1");

        EmployeeResponseDto emp2 = new EmployeeResponseDto();
        emp2.setId("id2");
        emp2.setName("Name2");

        Map<String, EmployeeResponseDto> map = new ConcurrentHashMap<>();
        map.put("id1", emp1);
        map.put("id2", emp2);

        cache.put(AppConstants.CACHE_KEY_ALL, map);

        EmployeeRequest request = new EmployeeRequest();
        request.setName("Name1");

        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setData(true);

        when(employeeClient.deleteEmployeeByName(request)).thenReturn(apiResponse);

        boolean deleted = employeeDataService.deleteEmployeeByName(request, "id1");

        assertTrue(deleted);

        Map<String, EmployeeResponseDto> cachedAfterDelete = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
        assertNotNull(cachedAfterDelete);
        assertFalse(cachedAfterDelete.containsKey("id1"));
        assertTrue(cachedAfterDelete.containsKey("id2"));

        verify(employeeClient, times(1)).deleteEmployeeByName(request);
    }

    @Test
    void testDeleteEmployeeByName_DeleteFails() {
        EmployeeResponseDto emp1 = new EmployeeResponseDto();
        emp1.setId("id1");
        emp1.setName("Name1");

        Map<String, EmployeeResponseDto> map = new ConcurrentHashMap<>();
        map.put("id1", emp1);
        cache.put(AppConstants.CACHE_KEY_ALL, map);

        EmployeeRequest request = new EmployeeRequest();
        request.setName("Name1");

        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setData(false);

        when(employeeClient.deleteEmployeeByName(request)).thenReturn(apiResponse);

        boolean deleted = employeeDataService.deleteEmployeeByName(request, "id1");

        assertFalse(deleted);
        Map<String, EmployeeResponseDto> cachedAfterDelete = cache.getIfPresent(AppConstants.CACHE_KEY_ALL);
        assertNotNull(cachedAfterDelete);
        assertTrue(cachedAfterDelete.containsKey("id1"));

        verify(employeeClient, times(1)).deleteEmployeeByName(request);
    }

    @Test
    void testToResponse_MapsCorrectly() {
        EmployeeDto dto = new EmployeeDto();
        dto.setId("idX");
        dto.setName("NameX");
        dto.setAge(30);
        dto.setSalary(5000);
        dto.setTitle("TitleX");
        dto.setEmail("email@example.com");

        EmployeeResponseDto response = employeeDataService.toResponse(dto);

        assertEquals("idX", response.getId());
        assertEquals("NameX", response.getName());
        assertEquals(30, response.getAge());
        assertEquals(5000, response.getSalary());
        assertEquals("TitleX", response.getTitle());
        assertEquals("email@example.com", response.getEmail());
    }
}

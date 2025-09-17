# Employee API Implementation
This app communicates with the mock APIs to implement the expected functionality


### Pre-requisites
Start the mock endpoint server using below command:
Start **Server** Spring Boot application
`./gradlew server:bootRun`

### Getting Started with API server invocation
Start **Server** Spring Boot application

1. Build the api using below command:
`./gradlew api:build`

2. Run the api using below command:
`./gradlew api:bootRun`

### Endpoints
The details about endpoints are available at - http://localhost:8111/swagger-ui/index.html

These endpoints can be used to test the functionality

## Assumptions while doing assignment:
1. The mock API available at port 8112 will only be used by EmployeeAPI client as the responses are cached considering that in mind
2. The size of the employees is not going to be significant as the current in memory cache is chosen keeping that in mind



# Assignment details:
# Implement this API

#### In this assessment you will be tasked with filling out the functionality of different methods that will be listed further down.

These methods will require some level of API interactions with Mock Employee API at http://localhost:8112/api/v1/employee.

Please keep the following in mind when doing this assessment:
* clean coding practices
* test driven development
* logging
* scalability

### Endpoints to implement

_See `com.reliaquest.api.controller.IEmployeeController` for details._

getAllEmployees()

    output - list of employees
    description - this should return all employees

getEmployeesByNameSearch(...)

    path input - name fragment
    output - list of employees
    description - this should return all employees whose name contains or matches the string input provided

getEmployeeById(...)

    path input - employee ID
    output - employee
    description - this should return a single employee

getHighestSalaryOfEmployees()

    output - integer of the highest salary
    description - this should return a single integer indicating the highest salary of amongst all employees

getTop10HighestEarningEmployeeNames()

    output - list of employees
    description - this should return a list of the top 10 employees based off of their salaries

createEmployee(...)

    body input - attributes necessary to create an employee
    output - employee
    description - this should return a single employee, if created, otherwise error

deleteEmployeeById(...)

    path input - employee ID
    output - name of the employee
    description - this should delete the employee with specified id given, otherwise error

### Testing
Please include proper integration and/or unit tests.

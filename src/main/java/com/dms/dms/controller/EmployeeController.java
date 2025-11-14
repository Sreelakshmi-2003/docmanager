package com.dms.dms.controller;

import com.dms.dms.dto.EmployeeRequest;
import com.dms.dms.entity.Department;
import com.dms.dms.entity.Employee;
import com.dms.dms.entity.Role;
import com.dms.dms.exception.ResourceNotFoundException;
import com.dms.dms.exception.DuplicateResourceException;
import com.dms.dms.service.EmployeeService;
import com.dms.dms.repository.DepartmentRepository;
import com.dms.dms.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin APIs", description = "Endpoints for administrative operations like managing employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final RoleRepository roleRepo;
    private final DepartmentRepository departmentRepo;

   @PostMapping("/create-employee")
public ResponseEntity<?> createEmployee(@RequestBody EmployeeRequest request) {

    try {
        Role empRole = roleRepo.findByRoleName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE role not found"));

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepo.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with ID: " + request.getDepartmentId()));
        }

        Employee newEmp = Employee.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .password(request.getPassword())
                .role(empRole)
                .department(dept)
                .build();

        Employee created = employeeService.addEmployee(newEmp);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Employee created successfully");

    } catch (DuplicateResourceException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());

    } catch (ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + e.getMessage());
    }
}
}
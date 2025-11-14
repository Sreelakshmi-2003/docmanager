package com.dms.dms.controller;

import com.dms.dms.dto.FolderDTO;
import com.dms.dms.entity.Department;
import com.dms.dms.entity.FolderType;
import com.dms.dms.exception.BadRequestException;
import com.dms.dms.exception.NoContentException;
import com.dms.dms.exception.ResourceNotFoundException;
import com.dms.dms.service.FolderService;
import com.dms.dms.repository.DepartmentRepository;
import com.dms.dms.repository.FolderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@Tag(name = "Folder APIs", description = "APIs to manage and fetch personal, department, and policy folders")
public class FolderController {

    private final FolderRepository folderRepository;
    private final DepartmentRepository departmentRepository;
    private final FolderService folderService;

    // ✅ Get Personal Folder
    @GetMapping("/personal/{employeeId}")
    @Operation(summary = "Get Personal Folder", description = "Fetches personal folders for a given employee using their employee ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal folders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid employee ID"),
            @ApiResponse(responseCode = "404", description = "No personal folders found for the employee")
    })
    public ResponseEntity<List<FolderDTO>> getPersonalFolder(
            @Parameter(description = "Employee ID to fetch personal folders for", required = true)
            @PathVariable String employeeId) {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new BadRequestException("Employee ID cannot be null or empty");
        }

        List<FolderDTO> folders = folderRepository
                .findDtoByEmployeeEmployeeIdAndFolderType(employeeId, FolderType.PERSONAL);

        if (folders.isEmpty()) {
            throw new NoContentException("No personal folders found for employee ID: " + employeeId);
        }

        return ResponseEntity.ok(folders);
    }

    // ✅ Get Department Folders
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get Department Folders", description = "Fetches all department folders for a specific department ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department folders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid department ID"),
            @ApiResponse(responseCode = "404", description = "Department not found or no folders available")
    })
    public ResponseEntity<List<FolderDTO>> getDepartmentFolders(
            @Parameter(description = "Department ID to fetch folders for", required = true)
            @PathVariable Integer departmentId) {

        if (departmentId == null || departmentId <= 0) {
            throw new BadRequestException("Invalid department ID");
        }

        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + departmentId));

        List<FolderDTO> folders = folderRepository
                .findDtoByDepartmentAndFolderType(dept, FolderType.DEPARTMENT);

        if (folders.isEmpty()) {
            throw new NoContentException("No department folders found for department ID: " + departmentId);
        }

        return ResponseEntity.ok(folders);
    }

    // ✅ Get Company Policy Folders
    @GetMapping("/policy")
    @Operation(summary = "Get Company Policy Folders", description = "Fetches all folders containing company policies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company policy folders retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No company policy folders found")
    })
    public ResponseEntity<List<FolderDTO>> getPolicyFolders() {
        List<FolderDTO> folders = folderRepository.findDtoByFolderType(FolderType.COMPANY_POLICY);

        if (folders.isEmpty()) {
            throw new NoContentException("No company policy folders found");
        }

        return ResponseEntity.ok(folders);
    }

    // ✅ Get All Accessible Folders for an Employee
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/accessible")
    @Operation(summary = "Get Accessible Folders", description = "Fetches all folders accessible to a given employee based on permissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accessible folders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid employee ID"),
            @ApiResponse(responseCode = "404", description = "No accessible folders found for the employee"),
            @ApiResponse(responseCode = "403", description = "Access denied — only admins can view this"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<FolderDTO>> getAccessibleFolders(
            @Parameter(description = "Employee ID to fetch accessible folders for", required = true)
            @RequestParam String employeeId) {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new BadRequestException("Employee ID is required");
        }

        List<FolderDTO> folders = folderService.getAccessibleFolders(employeeId);

        if (folders == null || folders.isEmpty()) {
            throw new NoContentException("No accessible folders found for employee ID: " + employeeId);
        }

        return ResponseEntity.ok(folders);
    }
}

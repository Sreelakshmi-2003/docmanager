package com.dms.dms.service;

import com.dms.dms.entity.Department;
import com.dms.dms.entity.Employee;
import com.dms.dms.entity.Folder;
import com.dms.dms.dto.FolderDTO;
import com.dms.dms.entity.FolderType;
import com.dms.dms.exception.BadRequestException;
import com.dms.dms.exception.ResourceNotFoundException;
import com.dms.dms.repository.DepartmentRepository;
import com.dms.dms.repository.EmployeeRepository;
import com.dms.dms.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final FolderRepository folderRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Employee addEmployee(Employee employee) {
        // Validate required fields
        if (employee.getEmployeeId() == null || employee.getEmployeeId().isBlank()) {
            throw new BadRequestException("Employee ID is required");
        }
        if (employee.getPassword() == null || employee.getPassword().isBlank()) {
            throw new BadRequestException("Password cannot be empty");
        }

        // Prevent duplicate employee ID
       if (employeeRepository.findByEmployeeId(employee.getEmployeeId()).isPresent()) {
    throw new BadRequestException("Employee ID '" + employee.getEmployeeId() + "' already exists");
}


        // Encode password securely
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        // Save employee to DB
        Employee savedEmployee = employeeRepository.save(employee);

        // Create personal folder for employee
        Folder personalFolder = Folder.builder()
                .folderName(savedEmployee.getName() + "_Personal")
                .folderType(FolderType.PERSONAL)
                .employee(savedEmployee)
                .build();
        folderRepository.save(personalFolder);

        // Ensure default department folders exist
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            throw new ResourceNotFoundException("No departments found while creating employee folders");
        }

        for (Department dept : departments) {
            List<FolderDTO> deptFolders =
                    folderRepository.findDtoByDepartmentAndFolderType(dept, FolderType.DEPARTMENT);
            if (deptFolders.isEmpty()) {
                Folder newDeptFolder = Folder.builder()
                        .folderName(dept.getName() + "_Dept")
                        .folderType(FolderType.DEPARTMENT)
                        .department(dept)
                        .build();
                folderRepository.save(newDeptFolder);
            }
        }

        // Ensure company policy folder exists
        List<FolderDTO> policyFolders = folderRepository.findDtoByFolderType(FolderType.COMPANY_POLICY);
        if (policyFolders.isEmpty()) {
            Folder newPolicyFolder = Folder.builder()
                    .folderName("Company_Policy")
                    .folderType(FolderType.COMPANY_POLICY)
                    .build();
            folderRepository.save(newPolicyFolder);
        }

        return savedEmployee;
    }
}

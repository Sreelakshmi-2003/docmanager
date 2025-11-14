package com.dms.dms.config;

import com.dms.dms.entity.Department;
import com.dms.dms.entity.Employee;
import com.dms.dms.entity.Role;
import com.dms.dms.entity.Folder;
import com.dms.dms.entity.FolderType;
import com.dms.dms.dto.FolderDTO;
import com.dms.dms.repository.DepartmentRepository;
import com.dms.dms.repository.FolderRepository;
import com.dms.dms.repository.EmployeeRepository;
import com.dms.dms.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRolesAndAdmin(RoleRepository roleRepository,
                                        EmployeeRepository empRepo,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            // Ensure roles exist
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .roleName("ADMIN")
                                    .build()
                    ));

            Role empRole = roleRepository.findByRoleName("EMPLOYEE")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .roleName("EMPLOYEE")
                                    .build()
                    ));

            // Create default admin if not exists
            String adminId = "ADMIN001";
            if (!empRepo.findByEmployeeId(adminId).isPresent()) {
                Employee admin = Employee.builder()
                        .employeeId(adminId)
                        .name("System Admin")
                        .password(passwordEncoder.encode("123")) // hashed password
                        .role(adminRole)
                        .build();
                empRepo.save(admin);
                System.out.println("Created default admin: " + adminId + " / password '123' (hashed)");
            }
        };
    }

    @Bean
    CommandLineRunner initDepartments(DepartmentRepository departmentRepository) {
        return args -> {
            createIfNotExists(departmentRepository, "Marketing");
            createIfNotExists(departmentRepository, "Sales");
            createIfNotExists(departmentRepository, "Finance");

            System.out.println("Departments initialized: Marketing, Sales, Finance");
        };
    }

    private void createIfNotExists(DepartmentRepository departmentRepository, String deptName) {
        departmentRepository.findByName(deptName).orElseGet(() -> {
            Department dept = Department.builder()
                    .name(deptName)
                    .build();
            return departmentRepository.save(dept);
        });
    }

    @Bean
    CommandLineRunner initDefaultFolders(DepartmentRepository departmentRepository, FolderRepository folderRepository) {
        return args -> {
            // Default department folders
            departmentRepository.findAll().forEach(dept -> {
                if (folderRepository.findDtoByDepartmentAndFolderType(dept, FolderType.DEPARTMENT) == null) {
                    Folder deptFolder = Folder.builder()
                            .folderName(dept.getName() + " Dept")
                            .folderType(FolderType.DEPARTMENT)
                            .department(dept)
                            .build();
                    folderRepository.save(deptFolder);
                }
            });

            // Company policy folder (not tied to a department)
            if (folderRepository.findDtoByFolderType(FolderType.COMPANY_POLICY) == null) {
                Folder policyFolder = Folder.builder()
                        .folderName("Company Policy")
                        .folderType(FolderType.COMPANY_POLICY)
                        .build();
                folderRepository.save(policyFolder);
            }

            System.out.println("Default folders initialized");
        };
    }
}

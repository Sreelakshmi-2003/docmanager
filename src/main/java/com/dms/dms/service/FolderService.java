package com.dms.dms.service;

import com.dms.dms.dto.FolderDTO;
import com.dms.dms.entity.Employee;
import com.dms.dms.entity.FolderType;
import com.dms.dms.exception.NoContentException;
import com.dms.dms.exception.ResourceNotFoundException;
import com.dms.dms.repository.EmployeeRepository;
import com.dms.dms.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final EmployeeRepository employeeRepository;

    public List<FolderDTO> getAccessibleFolders(String employeeId) {
        // ✅ Validate and fetch employee
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        String roleName = emp.getRole().getRoleName().toUpperCase();

        // ✅ Admin can access all folders
        if ("ADMIN".equals(roleName)) {
            List<FolderDTO> allFolders = folderRepository.findAllDto();
            if (allFolders.isEmpty()) {
                throw new NoContentException("No folders available for admin view");
            }
            return allFolders;
        }

        Integer deptId = emp.getDepartment() != null ? emp.getDepartment().getId() : null;

        // ✅ Common folders visible to all employees (e.g., company policy)
        List<FolderType> commonTypes = List.of(FolderType.COMPANY_POLICY);

        // ✅ Fetch folders: department + company policy + personal (filtered by employeeId)
        List<FolderDTO> accessible = folderRepository.findAccessibleFoldersForEmployee(
                deptId,
                emp.getEmployeeId(),
                commonTypes
        );

        if (accessible == null || accessible.isEmpty()) {
            throw new NoContentException("No accessible folders found for employee ID: " + employeeId);
        }

        return accessible;
    }

    public void deleteFolder(Integer folderId) {
    folderRepository.deleteById(folderId);
}

}

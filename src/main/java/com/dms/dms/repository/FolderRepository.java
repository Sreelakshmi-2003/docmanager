package com.dms.dms.repository;

import com.dms.dms.dto.FolderDTO;
import com.dms.dms.entity.Department;
import com.dms.dms.entity.Folder;
import com.dms.dms.entity.FolderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Integer> {

    // ✅ Fetch all folders for a specific employee and folder type (personal, etc.)
    @Query("""
        SELECT new com.dms.dms.dto.FolderDTO(f.id, f.folderName, f.folderType)
        FROM Folder f
        WHERE f.employee.employeeId = :employeeId AND f.folderType = :folderType
    """)
    List<FolderDTO> findDtoByEmployeeEmployeeIdAndFolderType(@Param("employeeId") String employeeId,
                                                             @Param("folderType") FolderType folderType);


    // ✅ Admin use — fetch all folders (returns DTOs to avoid lazy-loading)
    @Query("""
        SELECT new com.dms.dms.dto.FolderDTO(f.id, f.folderName, f.folderType)
        FROM Folder f
    """)
    List<FolderDTO> findAllDto();


    // ✅ Employee use — fetch department, policy, and own personal folder
    @Query("""
        SELECT new com.dms.dms.dto.FolderDTO(f.id, f.folderName, f.folderType)
        FROM Folder f
        WHERE 
            (f.department.id = :deptId)
            OR (f.folderType IN :commonTypes)
            OR (f.folderType = com.dms.dms.entity.FolderType.PERSONAL AND f.employee.employeeId = :employeeId)
    """)
    List<FolderDTO> findAccessibleFoldersForEmployee(@Param("deptId") Integer deptId,
                                                     @Param("employeeId") String employeeId,
                                                     @Param("commonTypes") List<FolderType> commonTypes);


    // ✅ Fetch all folders for a department and folder type
    @Query("""
        SELECT new com.dms.dms.dto.FolderDTO(f.id, f.folderName, f.folderType)
        FROM Folder f
        WHERE f.department = :department AND f.folderType = :folderType
    """)
    List<FolderDTO> findDtoByDepartmentAndFolderType(@Param("department") Department department,
                                                     @Param("folderType") FolderType folderType);


    // ✅ Fetch folders by a specific folder type (like COMPANY_POLICY)
    @Query("""
        SELECT new com.dms.dms.dto.FolderDTO(f.id, f.folderName, f.folderType)
        FROM Folder f
        WHERE f.folderType = :folderType
    """)
    List<FolderDTO> findDtoByFolderType(@Param("folderType") FolderType folderType);
}

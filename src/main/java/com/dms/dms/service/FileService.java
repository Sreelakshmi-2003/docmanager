package com.dms.dms.service;

import com.dms.dms.dto.FileDTO;
import com.dms.dms.entity.Employee;
import com.dms.dms.entity.FileEntity;
import com.dms.dms.entity.Folder;
import com.dms.dms.exception.BadRequestException;
import com.dms.dms.exception.ResourceNotFoundException;
import com.dms.dms.repository.EmployeeRepository;
import com.dms.dms.repository.FileRepository;
import com.dms.dms.repository.FolderRepository;
import com.dms.dms.util.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    private final String uploadDir = "./uploads";

    // Upload file
    public FileDTO uploadFile(Integer folderId, String uploaderId, String fileCategory, MultipartFile file,
                              String ip, String userAgent) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with ID: " + folderId));
        Employee uploader = employeeRepository.findByEmployeeId(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploader not found with ID: " + uploaderId));

        String extension = getFileExtension(file.getOriginalFilename());
        String physicalName = uploaderId + "_" + UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path targetPath = Paths.get(uploadDir).resolve(physicalName).normalize();
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        FileEntity entity = FileEntity.builder()
                .fileName(file.getOriginalFilename())
                .physicalName(physicalName)
                .fileUrl("/uploads/" + physicalName)
                .fileCategory(fileCategory)
                .folder(folder)
                .uploader(uploader)
                .uploadDate(Instant.now())
                .build();

        FileEntity saved = fileRepository.save(entity);

        auditLogService.log(
                uploaderId,
                "UPLOAD",
                "FILE",
                saved.getId().toString(),
                "Uploaded file: " + saved.getFileName(),
                ip,
                userAgent
        );

        return FileMapper.toDTO(saved);
    }

    // List files in folder
    public List<FileDTO> getFilesByFolder(Integer folderId, String userId, String ip, String userAgent) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with ID: " + folderId));

        List<FileDTO> files = fileRepository.findAll()
                .stream()
                .filter(f -> f.getFolder() != null && f.getFolder().getId().equals(folderId))
                .map(FileMapper::toDTO)
                .collect(Collectors.toList());

        files.forEach(f -> auditLogService.log(
                userId,
                "VIEW",
                "FILE",
                f.getId().toString(),
                "Viewed file listing: " + f.getFileName(),
                ip,
                userAgent
        ));

        return files;
    }

    // Download file
    public Resource downloadFile(Integer fileId, String userId, String ip, String userAgent) {
        FileEntity file = getFileById(fileId);

        Path filePath = Paths.get(uploadDir).resolve(file.getPhysicalName()).normalize();
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("File not found on disk: " + file.getFileName());
        }

        auditLogService.log(
                userId,
                "DOWNLOAD",
                "FILE",
                file.getId().toString(),
                "Downloaded file: " + file.getFileName(),
                ip,
                userAgent
        );

        return new FileSystemResource(filePath.toFile());
    }

    // Delete file
    public void deleteFile(Integer fileId, String userId, String ip, String userAgent) throws IOException {
        FileEntity entity = getFileById(fileId);

        Path filePath = Paths.get(uploadDir).resolve(entity.getPhysicalName()).normalize();
        Files.deleteIfExists(filePath);
        fileRepository.delete(entity);

        auditLogService.log(
                userId,
                "DELETE",
                "FILE",
                entity.getId().toString(),
                "Deleted file: " + entity.getFileName(),
                ip,
                userAgent
        );
    }

    // Get file by ID
    public FileEntity getFileById(Integer fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public void deleteFilesByFolderId(Integer folderId) throws IOException {

    List<FileEntity> files = fileRepository.findAll()
            .stream()
            .filter(f -> f.getFolder() != null && f.getFolder().getId().equals(folderId))
            .toList();

    for (FileEntity file : files) {
        Path filePath = Paths.get(uploadDir).resolve(file.getPhysicalName()).normalize();
        Files.deleteIfExists(filePath);
        fileRepository.delete(file);
    }
}

}

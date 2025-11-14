package com.dms.dms.controller;

import com.dms.dms.dto.FileDTO;
import com.dms.dms.entity.AuditLog;
import com.dms.dms.entity.FileEntity;
import com.dms.dms.service.FolderService;
import com.dms.dms.exception.ResourceNotFoundException;
import com.dms.dms.service.AuditLogService;
import com.dms.dms.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File APIs", description = "APIs to upload, download, delete files and view audit logs")
public class FileController {

    private final FileService fileService;
    private final AuditLogService auditLogService;
    private final FolderService folderService;


    // -----------------------------
    // ✅ Upload File
    // -----------------------------
    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Upload a file to a specific folder with category. Logs uploader details and IP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing input data"),
            @ApiResponse(responseCode = "404", description = "Folder or uploader not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<?> uploadFile(
            @Parameter(description = "ID of the folder to upload file to", required = true)
            @RequestParam("folderId") Integer folderId,
            @Parameter(description = "ID of the uploader", required = true)
            @RequestParam("uploaderId") String uploaderId,
            @Parameter(description = "Category of the file", required = true)
            @RequestParam("fileCategory") String fileCategory,
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            if (folderId == null || uploaderId == null || uploaderId.isEmpty() ||
                fileCategory == null || fileCategory.isEmpty() ||
                file == null || file.isEmpty()) {
                throw new IllegalArgumentException("All fields (folderId, uploaderId, fileCategory, file) are required.");
            }

            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            FileDTO savedFile = fileService.uploadFile(folderId, uploaderId, fileCategory, file, ip, userAgent);
            return ResponseEntity.ok(savedFile);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", e.getMessage()));
        }
    }

    // -----------------------------
    // ✅ List Files
    // -----------------------------
    @GetMapping("/folder/{folderId}")
    @Operation(summary = "List files in a folder", description = "Lists all files in a folder and logs who viewed them.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Files listed successfully"),
            @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    public ResponseEntity<?> listFiles(
            @Parameter(description = "ID of the folder", required = true)
            @PathVariable Integer folderId,
            @Parameter(description = "ID of the user viewing the files", required = false)
            @RequestParam(required = false, defaultValue = "unknownUser") String userId,
            HttpServletRequest request) {

        try {
            if (folderId == null) throw new IllegalArgumentException("Folder ID is required.");

            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            List<FileDTO> files = fileService.getFilesByFolder(folderId, userId, ip, userAgent);
            return ResponseEntity.ok(files);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", e.getMessage()));
        }
    }

    // -----------------------------
    // ✅ Download File
    // -----------------------------
    @GetMapping("/download/{fileId}")
    @Operation(summary = "Download a file", description = "Download a file by its ID. Logs the download action.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<?> downloadFile(
            @Parameter(description = "ID of the file to download", required = true)
            @PathVariable Integer fileId,
            @Parameter(description = "ID of the user downloading the file", required = false)
            @RequestParam(required = false, defaultValue = "unknownUser") String userId,
            HttpServletRequest request) {

        try {
            if (fileId == null) throw new IllegalArgumentException("File ID is required.");

            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            FileEntity file = fileService.getFileById(fileId);
            Resource resource = fileService.downloadFile(fileId, userId, ip, userAgent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                    .body(resource);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", e.getMessage()));
        }
    }

    // -----------------------------
    // ✅ Delete File (Admin Only)
    // -----------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file", description = "Deletes a file by its ID and logs the delete action. Only Admins can perform this operation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied — only admins can delete files"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<?> deleteFile(
            @Parameter(description = "ID of the file to delete", required = true)
            @PathVariable Integer fileId,
            @Parameter(description = "ID of the user deleting the file", required = false)
            @RequestParam(required = false, defaultValue = "unknownUser") String userId,
            HttpServletRequest request) {

        try {
            if (fileId == null) throw new IllegalArgumentException("File ID is required.");

            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            fileService.deleteFile(fileId, userId, ip, userAgent);

            return ResponseEntity.ok(Map.of("message", "File deleted successfully."));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", e.getMessage()));
        }
    }

    // -----------------------------
    // ✅ File Audit Logs
    // -----------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit/{fileId}")
    @Operation(summary = "Get audit logs for a file", description = "Fetches all audit logs associated with a file ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No audit logs found")
    })
    public ResponseEntity<?> getAuditLogs(
            @Parameter(description = "ID of the file", required = true)
            @PathVariable Integer fileId) {

        try {
            if (fileId == null) throw new IllegalArgumentException("File ID is required.");

            List<AuditLog> logs = auditLogService.getLogsByFileId(fileId.toString());
            if (logs.isEmpty()) {
                throw new ResourceNotFoundException("No audit logs found for file ID: " + fileId);
            }

            return ResponseEntity.ok(logs);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/folder/{folderId}")
@Operation(summary = "Delete a folder (Admin only)",
        description = "Deletes a folder by ID and all files inside it. Only admins can perform this operation.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Folder and files deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied — only admins can delete folders"),
        @ApiResponse(responseCode = "404", description = "Folder not found")
})
public ResponseEntity<?> deleteFolderWithFiles(
        @Parameter(description = "ID of the folder to delete", required = true)
        @PathVariable Integer folderId,
        HttpServletRequest request) {

    try {
        if (folderId == null) {
            throw new IllegalArgumentException("Folder ID is required.");
        }

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Delete all files inside the folder
        fileService.deleteFilesByFolderId(folderId);

        // Delete the folder itself
        folderService.deleteFolder(folderId);

        // Log the admin action
        auditLogService.log(
                "ADMIN",
                "DELETE",
                "FOLDER",
                folderId.toString(),
                "Admin deleted folder and all its files",
                ip,
                userAgent
        );

        return ResponseEntity.ok(Map.of(
                "message", "Folder and all files deleted successfully."
        ));

    } catch (ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Not Found", "message", e.getMessage()));

    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Bad Request", "message", e.getMessage()));

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error", "message", e.getMessage()));
    }
}

}

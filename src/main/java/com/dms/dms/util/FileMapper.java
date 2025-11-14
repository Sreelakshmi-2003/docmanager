package com.dms.dms.util;

import com.dms.dms.dto.FileDTO;
import com.dms.dms.entity.FileEntity;

public class FileMapper {

    public static FileDTO toDTO(FileEntity file) {
        if (file == null) return null;
        return FileDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .physicalName(file.getPhysicalName())
                .fileUrl(file.getFileUrl())
                .fileCategory(file.getFileCategory())
                .uploadDate(file.getUploadDate())
                .uploaderId(file.getUploader() != null ? file.getUploader().getEmployeeId() : null)
                .uploaderName(file.getUploader() != null ? file.getUploader().getName() : null)
                .folderId(file.getFolder() != null ? file.getFolder().getId() : null)
                .build();
    }
}

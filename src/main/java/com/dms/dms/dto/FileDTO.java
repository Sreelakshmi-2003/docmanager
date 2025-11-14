package com.dms.dms.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {
    private Integer id;
    private String fileName;
    private String physicalName;
    private String fileUrl;
    private String fileCategory;
    private Instant uploadDate;
    private String uploaderId;
    private String uploaderName;
    private Integer folderId;
}

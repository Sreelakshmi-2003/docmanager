package com.dms.dms.dto;

import com.dms.dms.entity.FolderType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FolderDTO {
    private Integer id;
    private String folderName;
    private FolderType folderType;
}

package com.dms.dms.repository;

import com.dms.dms.entity.FileEntity;
import com.dms.dms.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, Integer> {
    List<FileEntity> findByFolder(Folder folder);
}

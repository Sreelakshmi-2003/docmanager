package com.dms.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Column(name = "file_category")
    private String fileCategory;


    // uploader_id -> employees.employee_id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploader_id", referencedColumnName = "employee_id", nullable = false)
    private Employee uploader;

    @Column(name = "file_name")
    private String fileName; // original filename

    @Column(name = "physical_name", unique = true)
    private String physicalName; // system filename (EMP001_UUID.pdf)

    @Column(name = "file_url")
    private String fileUrl; // server path or storage URI

    @Column(name = "upload_date", updatable = false)
    private Instant uploadDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "last_opened_by", referencedColumnName = "employee_id")
    private Employee lastOpenedBy;

    @Column(name = "last_opened_at")
    private Instant lastOpenedAt;

   @PrePersist
public void prePersist() {
    if (this.uploadDate == null) this.uploadDate = Instant.now();
}
}

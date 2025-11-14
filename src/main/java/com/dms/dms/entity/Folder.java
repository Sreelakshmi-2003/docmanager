package com.dms.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment integer
    @Column(name = "id")
    private Integer id; 

    @Column(name = "folder_name")
    private String folderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "folder_type", nullable = false)
    private FolderType folderType;

    // nullable FK → departments.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // nullable FK → employees.employee_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}

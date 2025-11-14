package com.dms.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    // user_id references employees.employee_id (can be nullable for system actions)
    @Column(name = "user_id")
    private String userId;

    @Column(name = "action_type")
    private String actionType; // UPLOAD, DOWNLOAD, VIEW, DELETE, LOGIN, ADD_USER, etc.

    @Column(name = "entity_type")
    private String entityType; // FILE, FOLDER, USER, DEPARTMENT

    @Column(name = "entity_id")
    private String entityId; // UUID or varchar

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "timestamp", updatable = false)
    private Instant timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.timestamp == null) this.timestamp = Instant.now();
    }
}

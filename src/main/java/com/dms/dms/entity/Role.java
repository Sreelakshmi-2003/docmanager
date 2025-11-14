package com.dms.dms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @Column(name = "role_id")
    private Integer roleId; // changed from UUID to Integer

    @Column(name = "role_name", unique = true, nullable = false)
    private String roleName;
}

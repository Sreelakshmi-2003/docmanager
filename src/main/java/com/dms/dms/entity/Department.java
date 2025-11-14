package com.dms.dms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment integer
    @Column(name = "id")
    private Integer id;  // changed from UUID to Integer

    @Column(name = "name", unique = true, nullable = false)
    private String name;
}

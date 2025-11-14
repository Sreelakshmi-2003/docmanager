package com.dms.dms.dto;

import lombok.Data;

@Data
public class EmployeeRequest {
    private String employeeId;     // ID for the new employee
    private String name;           // Name of the employee
    private String password;       // Plain password; will be encoded
    private Integer departmentId;  // Optional, if department is used
}

package com.dms.dms.service;

import com.dms.dms.entity.Employee;
import com.dms.dms.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class EmployeeDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch employee by employeeId
        Employee emp = employeeRepository.findByEmployeeId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Default to EMPLOYEE if role is null
        String roleName = emp.getRole() != null ? emp.getRole().getRoleName() : "EMPLOYEE";

        // Add ROLE_ prefix for Spring Security
        return new User(
                emp.getEmployeeId(),
                emp.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
    }
}

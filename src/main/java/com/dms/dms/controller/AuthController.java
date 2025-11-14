package com.dms.dms.controller;

import com.dms.dms.config.JwtService;
import com.dms.dms.entity.Employee;
import com.dms.dms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String employeeId = loginRequest.get("employeeId");
        String password = loginRequest.get("password");

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(employeeId, password)
            );

            // Find employee details
            Employee emp = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Invalid employee ID"));

            // Generate token
            String token = jwtService.generateToken(emp.getEmployeeId(), emp.getRole().getRoleName());

            // Return response
            return ResponseEntity.ok(Map.of(
                    "employeeId", emp.getEmployeeId(),
                    "accessToken", token,
                    "role", emp.getRole().getRoleName()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "error", "Unauthorized",
                    "message", "Invalid username or password"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage()
            ));
        }
    }
}

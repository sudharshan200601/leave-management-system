package com.leave.management.security.controller;

import com.leave.management.employee.model.Employee;
import com.leave.management.employee.repository.EmployeeRepository;
import com.leave.management.security.JwtTokenProvider;
import com.leave.management.security.dto.LoginRequestDto;
import com.leave.management.security.dto.LoginResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmployeeRepository employeeRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          EmployeeRepository employeeRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String token = tokenProvider.generateToken(employee);

        LoginResponseDto response = LoginResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .teamId(employee.getTeamId())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .build();

        return ResponseEntity.ok(response);
    }
}

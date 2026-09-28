package com.leave.management.employee.dto;

import com.leave.management.employee.model.Role;
import java.time.LocalDate;

public class EmployeeAdminDto {
    private Long id;
    private String name;
    private String email;
    private String plainPassword;
    private Role role;
    private Long managerId;
    private Long teamId;
    private LocalDate joinDate;

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPlainPassword() { return plainPassword; }
    public void setPlainPassword(String plainPassword) { this.plainPassword = plainPassword; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
}

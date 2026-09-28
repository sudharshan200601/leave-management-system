package com.leave.management.security.dto;

import com.leave.management.employee.model.Role;

public class LoginResponseDto {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long teamId;
    private Long managerId;

    public LoginResponseDto() {}

    public LoginResponseDto(String token, String tokenType, Long id, String name, String email, Role role, Long teamId, Long managerId) {
        this.token = token;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.teamId = teamId;
        this.managerId = managerId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String tokenType = "Bearer";
        private Long id;
        private String name;
        private String email;
        private Role role;
        private Long teamId;
        private Long managerId;

        public Builder token(String token) { this.token = token; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder teamId(Long teamId) { this.teamId = teamId; return this; }
        public Builder managerId(Long managerId) { this.managerId = managerId; return this; }

        public LoginResponseDto build() {
            return new LoginResponseDto(token, tokenType, id, name, email, role, teamId, managerId);
        }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
}

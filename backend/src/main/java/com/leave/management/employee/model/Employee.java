package com.leave.management.employee.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "plain_password")
    private String plainPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private LocalDate joinDate;

    public Employee() {}

    public Employee(Long id, String name, String email, String passwordHash, String plainPassword, Role role, Employee manager, Long teamId, LocalDate joinDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.plainPassword = plainPassword;
        this.role = role;
        this.manager = manager;
        this.teamId = teamId;
        this.joinDate = joinDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String passwordHash;
        private String plainPassword;
        private Role role;
        private Employee manager;
        private Long teamId;
        private LocalDate joinDate;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder plainPassword(String plainPassword) { this.plainPassword = plainPassword; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder manager(Employee manager) { this.manager = manager; return this; }
        public Builder teamId(Long teamId) { this.teamId = teamId; return this; }
        public Builder joinDate(LocalDate joinDate) { this.joinDate = joinDate; return this; }

        public Employee build() {
            return new Employee(id, name, email, passwordHash, plainPassword, role, manager, teamId, joinDate);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPlainPassword() { return plainPassword; }
    public void setPlainPassword(String plainPassword) { this.plainPassword = plainPassword; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
}

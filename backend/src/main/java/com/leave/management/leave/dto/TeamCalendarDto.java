package com.leave.management.leave.dto;

import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.model.LeaveType;
import java.time.LocalDate;

public class TeamCalendarDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long teamId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private LeaveType leaveType;

    public TeamCalendarDto() {}

    public TeamCalendarDto(Long id, Long employeeId, String employeeName, Long teamId,
                           LocalDate startDate, LocalDate endDate, LeaveStatus status, LeaveType leaveType) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.teamId = teamId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.leaveType = leaveType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private Long teamId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LeaveStatus status;
        private LeaveType leaveType;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder employeeId(Long employeeId) { this.employeeId = employeeId; return this; }
        public Builder employeeName(String employeeName) { this.employeeName = employeeName; return this; }
        public Builder teamId(Long teamId) { this.teamId = teamId; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder status(LeaveStatus status) { this.status = status; return this; }
        public Builder leaveType(LeaveType leaveType) { this.leaveType = leaveType; return this; }

        public TeamCalendarDto build() {
            return new TeamCalendarDto(id, employeeId, employeeName, teamId, startDate, endDate, status, leaveType);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
}

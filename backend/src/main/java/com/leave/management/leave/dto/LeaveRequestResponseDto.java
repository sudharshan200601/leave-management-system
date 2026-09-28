package com.leave.management.leave.dto;

import com.leave.management.approval.dto.ApprovalStepDto;
import com.leave.management.leave.model.ConflictSeverity;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.model.LeaveType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LeaveRequestResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long teamId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal workingDays;
    private String reason;
    private LeaveStatus status;
    private boolean conflictFlag;
    private ConflictSeverity conflictSeverity;
    private String conflictDetails;
    private LocalDateTime submittedAt;
    private LocalDateTime lastActionAt;
    private boolean escalated;
    private LocalDate compensatoryWorkingDate;
    private String odType;
    private String location;
    private String remarks;
    private String startTime;
    private String endTime;
    private String documentName;
    private String documentData;
    private List<ApprovalStepDto> approvalSteps;

    public LeaveRequestResponseDto() {}

    public LeaveRequestResponseDto(Long id, Long employeeId, String employeeName, String employeeEmail, Long teamId,
                                  LeaveType leaveType, LocalDate startDate, LocalDate endDate, BigDecimal workingDays,
                                  String reason, LeaveStatus status, boolean conflictFlag, ConflictSeverity conflictSeverity,
                                  String conflictDetails, LocalDateTime submittedAt, LocalDateTime lastActionAt,
                                  boolean escalated, LocalDate compensatoryWorkingDate, String documentName,
                                  String documentData, List<ApprovalStepDto> approvalSteps,
                                  String odType, String location, String remarks, String startTime, String endTime) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.teamId = teamId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.workingDays = workingDays;
        this.reason = reason;
        this.status = status;
        this.conflictFlag = conflictFlag;
        this.conflictSeverity = conflictSeverity;
        this.conflictDetails = conflictDetails;
        this.submittedAt = submittedAt;
        this.lastActionAt = lastActionAt;
        this.escalated = escalated;
        this.compensatoryWorkingDate = compensatoryWorkingDate;
        this.documentName = documentName;
        this.documentData = documentData;
        this.approvalSteps = approvalSteps;
        this.odType = odType;
        this.location = location;
        this.remarks = remarks;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeEmail;
        private Long teamId;
        private LeaveType leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal workingDays;
        private String reason;
        private LeaveStatus status;
        private boolean conflictFlag;
        private ConflictSeverity conflictSeverity;
        private String conflictDetails;
        private LocalDateTime submittedAt;
        private LocalDateTime lastActionAt;
        private boolean escalated;
        private LocalDate compensatoryWorkingDate;
        private String odType;
        private String location;
        private String remarks;
        private String startTime;
        private String endTime;
        private String documentName;
        private String documentData;
        private List<ApprovalStepDto> approvalSteps;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder employeeId(Long employeeId) { this.employeeId = employeeId; return this; }
        public Builder employeeName(String employeeName) { this.employeeName = employeeName; return this; }
        public Builder employeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; return this; }
        public Builder teamId(Long teamId) { this.teamId = teamId; return this; }
        public Builder leaveType(LeaveType leaveType) { this.leaveType = leaveType; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder workingDays(BigDecimal workingDays) { this.workingDays = workingDays; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder status(LeaveStatus status) { this.status = status; return this; }
        public Builder conflictFlag(boolean conflictFlag) { this.conflictFlag = conflictFlag; return this; }
        public Builder conflictSeverity(ConflictSeverity conflictSeverity) { this.conflictSeverity = conflictSeverity; return this; }
        public Builder conflictDetails(String conflictDetails) { this.conflictDetails = conflictDetails; return this; }
        public Builder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public Builder lastActionAt(LocalDateTime lastActionAt) { this.lastActionAt = lastActionAt; return this; }
        public Builder escalated(boolean escalated) { this.escalated = escalated; return this; }
        public Builder compensatoryWorkingDate(LocalDate compensatoryWorkingDate) { this.compensatoryWorkingDate = compensatoryWorkingDate; return this; }
        public Builder odType(String odType) { this.odType = odType; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder remarks(String remarks) { this.remarks = remarks; return this; }
        public Builder startTime(String startTime) { this.startTime = startTime; return this; }
        public Builder endTime(String endTime) { this.endTime = endTime; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder documentData(String documentData) { this.documentData = documentData; return this; }
        public Builder approvalSteps(List<ApprovalStepDto> approvalSteps) { this.approvalSteps = approvalSteps; return this; }

        public LeaveRequestResponseDto build() {
            return new LeaveRequestResponseDto(id, employeeId, employeeName, employeeEmail, teamId, leaveType,
                    startDate, endDate, workingDays, reason, status, conflictFlag, conflictSeverity, conflictDetails,
                    submittedAt, lastActionAt, escalated, compensatoryWorkingDate, documentName, documentData, approvalSteps,
                    odType, location, remarks, startTime, endTime);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getWorkingDays() { return workingDays; }
    public void setWorkingDays(BigDecimal workingDays) { this.workingDays = workingDays; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }
    public boolean isConflictFlag() { return conflictFlag; }
    public void setConflictFlag(boolean conflictFlag) { this.conflictFlag = conflictFlag; }
    public ConflictSeverity getConflictSeverity() { return conflictSeverity; }
    public void setConflictSeverity(ConflictSeverity conflictSeverity) { this.conflictSeverity = conflictSeverity; }
    public String getConflictDetails() { return conflictDetails; }
    public void setConflictDetails(String conflictDetails) { this.conflictDetails = conflictDetails; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getLastActionAt() { return lastActionAt; }
    public void setLastActionAt(LocalDateTime lastActionAt) { this.lastActionAt = lastActionAt; }
    public boolean isEscalated() { return escalated; }
    public void setEscalated(boolean escalated) { this.escalated = escalated; }
    public LocalDate getCompensatoryWorkingDate() { return compensatoryWorkingDate; }
    public void setCompensatoryWorkingDate(LocalDate compensatoryWorkingDate) { this.compensatoryWorkingDate = compensatoryWorkingDate; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentData() { return documentData; }
    public void setDocumentData(String documentData) { this.documentData = documentData; }
    public List<ApprovalStepDto> getApprovalSteps() { return approvalSteps; }
    public void setApprovalSteps(List<ApprovalStepDto> approvalSteps) { this.approvalSteps = approvalSteps; }
    public String getOdType() { return odType; }
    public void setOdType(String odType) { this.odType = odType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}

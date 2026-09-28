package com.leave.management.leave.model;

import com.leave.management.employee.model.Employee;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal workingDays;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @Column(nullable = false)
    private boolean conflictFlag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConflictSeverity conflictSeverity;

    @Column(columnDefinition = "TEXT")
    private String conflictDetails;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime lastActionAt;

    @Column(nullable = false)
    private boolean escalated;

    @Column
    private LocalDate compensatoryWorkingDate;

    @Column
    private String odType;

    @Column
    private String location;

    @Column
    private String remarks;

    @Column
    private String startTime;

    @Column
    private String endTime;

    @Column
    private String documentName;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String documentData;

    @Version
    private Long version;

    public LeaveRequest() {}

    public LeaveRequest(Long id, Employee employee, LeaveType leaveType, LocalDate startDate, LocalDate endDate,
                        BigDecimal workingDays, String reason, LeaveStatus status, boolean conflictFlag,
                        ConflictSeverity conflictSeverity, String conflictDetails, LocalDateTime submittedAt,
                        LocalDateTime lastActionAt, boolean escalated, LocalDate compensatoryWorkingDate,
                        String odType, String location, String remarks, String startTime, String endTime,
                        String documentName, String documentData, Long version) {
        this.id = id;
        this.employee = employee;
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
        this.odType = odType;
        this.location = location;
        this.remarks = remarks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.documentName = documentName;
        this.documentData = documentData;
        this.version = version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Employee employee;
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
        private Long version;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder employee(Employee employee) { this.employee = employee; return this; }
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
        public Builder version(Long version) { this.version = version; return this; }

        public LeaveRequest build() {
            return new LeaveRequest(id, employee, leaveType, startDate, endDate, workingDays, reason, status,
                    conflictFlag, conflictSeverity, conflictDetails, submittedAt, lastActionAt, escalated,
                    compensatoryWorkingDate, odType, location, remarks, startTime, endTime, documentName, documentData, version);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
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
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentData() { return documentData; }
    public void setDocumentData(String documentData) { this.documentData = documentData; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

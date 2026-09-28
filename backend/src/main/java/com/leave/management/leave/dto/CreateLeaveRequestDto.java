package com.leave.management.leave.dto;

import com.leave.management.leave.model.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateLeaveRequestDto {

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotBlank(message = "Reason is required")
    private String reason;

    private LocalDate compensatoryWorkingDate;

    private String odType;
    private String location;
    private String remarks;
    private String startTime;
    private String endTime;

    private String documentName;

    private String documentData;

    public CreateLeaveRequestDto() {}

    public CreateLeaveRequestDto(LocalDate startDate, LocalDate endDate, LeaveType leaveType, String reason) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.reason = reason;
    }

    public CreateLeaveRequestDto(LocalDate startDate, LocalDate endDate, LeaveType leaveType, String reason,
                                 LocalDate compensatoryWorkingDate, String documentName, String documentData,
                                 String odType, String location, String remarks, String startTime, String endTime) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.reason = reason;
        this.compensatoryWorkingDate = compensatoryWorkingDate;
        this.documentName = documentName;
        this.documentData = documentData;
        this.odType = odType;
        this.location = location;
        this.remarks = remarks;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDate getCompensatoryWorkingDate() { return compensatoryWorkingDate; }
    public void setCompensatoryWorkingDate(LocalDate compensatoryWorkingDate) { this.compensatoryWorkingDate = compensatoryWorkingDate; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentData() { return documentData; }
    public void setDocumentData(String documentData) { this.documentData = documentData; }
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

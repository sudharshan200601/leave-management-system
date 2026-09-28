package com.leave.management.swap.dto;

import java.time.LocalDate;

public class EligibleSwapDto {
    private Long targetEmployeeId;
    private String targetEmployeeName;
    private Long targetLeaveRequestId;
    private LocalDate startDate;
    private LocalDate endDate;

    public EligibleSwapDto(Long targetEmployeeId, String targetEmployeeName, Long targetLeaveRequestId, LocalDate startDate, LocalDate endDate) {
        this.targetEmployeeId = targetEmployeeId;
        this.targetEmployeeName = targetEmployeeName;
        this.targetLeaveRequestId = targetLeaveRequestId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getTargetEmployeeId() { return targetEmployeeId; }
    public void setTargetEmployeeId(Long targetEmployeeId) { this.targetEmployeeId = targetEmployeeId; }

    public String getTargetEmployeeName() { return targetEmployeeName; }
    public void setTargetEmployeeName(String targetEmployeeName) { this.targetEmployeeName = targetEmployeeName; }

    public Long getTargetLeaveRequestId() { return targetLeaveRequestId; }
    public void setTargetLeaveRequestId(Long targetLeaveRequestId) { this.targetLeaveRequestId = targetLeaveRequestId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}

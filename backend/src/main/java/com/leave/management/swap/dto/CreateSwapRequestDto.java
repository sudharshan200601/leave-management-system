package com.leave.management.swap.dto;

import java.time.LocalDate;

public class CreateSwapRequestDto {
    private Long requesterLeaveRequestId;
    private Long targetLeaveRequestId;
    private LocalDate targetNewStartDate;
    private LocalDate targetNewEndDate;

    public Long getRequesterLeaveRequestId() { return requesterLeaveRequestId; }
    public void setRequesterLeaveRequestId(Long requesterLeaveRequestId) { this.requesterLeaveRequestId = requesterLeaveRequestId; }

    public Long getTargetLeaveRequestId() { return targetLeaveRequestId; }
    public void setTargetLeaveRequestId(Long targetLeaveRequestId) { this.targetLeaveRequestId = targetLeaveRequestId; }

    public LocalDate getTargetNewStartDate() { return targetNewStartDate; }
    public void setTargetNewStartDate(LocalDate targetNewStartDate) { this.targetNewStartDate = targetNewStartDate; }

    public LocalDate getTargetNewEndDate() { return targetNewEndDate; }
    public void setTargetNewEndDate(LocalDate targetNewEndDate) { this.targetNewEndDate = targetNewEndDate; }
}

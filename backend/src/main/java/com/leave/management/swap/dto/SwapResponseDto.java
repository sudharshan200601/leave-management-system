package com.leave.management.swap.dto;

import com.leave.management.swap.model.SwapStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SwapResponseDto {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long targetEmployeeId;
    private String targetEmployeeName;
    private Long requesterLeaveRequestId;
    private Long targetLeaveRequestId;
    private LocalDate originalStartDate;
    private LocalDate originalEndDate;
    private LocalDate targetNewStartDate;
    private LocalDate targetNewEndDate;
    private SwapStatus status;
    private String coverageBefore;
    private String coverageAfter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public Long getTargetEmployeeId() { return targetEmployeeId; }
    public void setTargetEmployeeId(Long targetEmployeeId) { this.targetEmployeeId = targetEmployeeId; }

    public String getTargetEmployeeName() { return targetEmployeeName; }
    public void setTargetEmployeeName(String targetEmployeeName) { this.targetEmployeeName = targetEmployeeName; }

    public Long getRequesterLeaveRequestId() { return requesterLeaveRequestId; }
    public void setRequesterLeaveRequestId(Long requesterLeaveRequestId) { this.requesterLeaveRequestId = requesterLeaveRequestId; }

    public Long getTargetLeaveRequestId() { return targetLeaveRequestId; }
    public void setTargetLeaveRequestId(Long targetLeaveRequestId) { this.targetLeaveRequestId = targetLeaveRequestId; }

    public LocalDate getOriginalStartDate() { return originalStartDate; }
    public void setOriginalStartDate(LocalDate originalStartDate) { this.originalStartDate = originalStartDate; }

    public LocalDate getOriginalEndDate() { return originalEndDate; }
    public void setOriginalEndDate(LocalDate originalEndDate) { this.originalEndDate = originalEndDate; }

    public LocalDate getTargetNewStartDate() { return targetNewStartDate; }
    public void setTargetNewStartDate(LocalDate targetNewStartDate) { this.targetNewStartDate = targetNewStartDate; }

    public LocalDate getTargetNewEndDate() { return targetNewEndDate; }
    public void setTargetNewEndDate(LocalDate targetNewEndDate) { this.targetNewEndDate = targetNewEndDate; }

    public SwapStatus getStatus() { return status; }
    public void setStatus(SwapStatus status) { this.status = status; }

    public String getCoverageBefore() { return coverageBefore; }
    public void setCoverageBefore(String coverageBefore) { this.coverageBefore = coverageBefore; }

    public String getCoverageAfter() { return coverageAfter; }
    public void setCoverageAfter(String coverageAfter) { this.coverageAfter = coverageAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

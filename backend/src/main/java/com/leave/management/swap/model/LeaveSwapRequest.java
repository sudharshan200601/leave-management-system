package com.leave.management.swap.model;

import com.leave.management.employee.model.Employee;
import com.leave.management.leave.model.LeaveRequest;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_swap_requests")
public class LeaveSwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Employee requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_employee_id", nullable = false)
    private Employee targetEmployee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_leave_request_id", nullable = false)
    private LeaveRequest requesterLeaveRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_leave_request_id", nullable = false)
    private LeaveRequest targetLeaveRequest;

    @Column(nullable = false)
    private LocalDate targetNewStartDate;

    @Column(nullable = false)
    private LocalDate targetNewEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SwapStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public LeaveSwapRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getRequester() { return requester; }
    public void setRequester(Employee requester) { this.requester = requester; }

    public Employee getTargetEmployee() { return targetEmployee; }
    public void setTargetEmployee(Employee targetEmployee) { this.targetEmployee = targetEmployee; }

    public LeaveRequest getRequesterLeaveRequest() { return requesterLeaveRequest; }
    public void setRequesterLeaveRequest(LeaveRequest requesterLeaveRequest) { this.requesterLeaveRequest = requesterLeaveRequest; }

    public LeaveRequest getTargetLeaveRequest() { return targetLeaveRequest; }
    public void setTargetLeaveRequest(LeaveRequest targetLeaveRequest) { this.targetLeaveRequest = targetLeaveRequest; }

    public LocalDate getTargetNewStartDate() { return targetNewStartDate; }
    public void setTargetNewStartDate(LocalDate targetNewStartDate) { this.targetNewStartDate = targetNewStartDate; }

    public LocalDate getTargetNewEndDate() { return targetNewEndDate; }
    public void setTargetNewEndDate(LocalDate targetNewEndDate) { this.targetNewEndDate = targetNewEndDate; }

    public SwapStatus getStatus() { return status; }
    public void setStatus(SwapStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

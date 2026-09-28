package com.leave.management.approval.model;

import com.leave.management.employee.model.Employee;
import com.leave.management.leave.model.LeaveRequest;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_steps")
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalDecision decision;

    private String comment;

    @Column(nullable = false)
    private LocalDateTime actedAt;

    public ApprovalStep() {}

    public ApprovalStep(Long id, LeaveRequest leaveRequest, ApprovalLevel level, Employee approver,
                        ApprovalDecision decision, String comment, LocalDateTime actedAt) {
        this.id = id;
        this.leaveRequest = leaveRequest;
        this.level = level;
        this.approver = approver;
        this.decision = decision;
        this.comment = comment;
        this.actedAt = actedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private LeaveRequest leaveRequest;
        private ApprovalLevel level;
        private Employee approver;
        private ApprovalDecision decision;
        private String comment;
        private LocalDateTime actedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder leaveRequest(LeaveRequest leaveRequest) { this.leaveRequest = leaveRequest; return this; }
        public Builder level(ApprovalLevel level) { this.level = level; return this; }
        public Builder approver(Employee approver) { this.approver = approver; return this; }
        public Builder decision(ApprovalDecision decision) { this.decision = decision; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }
        public Builder actedAt(LocalDateTime actedAt) { this.actedAt = actedAt; return this; }

        public ApprovalStep build() {
            return new ApprovalStep(id, leaveRequest, level, approver, decision, comment, actedAt);
        }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LeaveRequest getLeaveRequest() { return leaveRequest; }
    public void setLeaveRequest(LeaveRequest leaveRequest) { this.leaveRequest = leaveRequest; }
    public ApprovalLevel getLevel() { return level; }
    public void setLevel(ApprovalLevel level) { this.level = level; }
    public Employee getApprover() { return approver; }
    public void setApprover(Employee approver) { this.approver = approver; }
    public ApprovalDecision getDecision() { return decision; }
    public void setDecision(ApprovalDecision decision) { this.decision = decision; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getActedAt() { return actedAt; }
    public void setActedAt(LocalDateTime actedAt) { this.actedAt = actedAt; }
}

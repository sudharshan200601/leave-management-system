package com.leave.management.approval.dto;

import com.leave.management.approval.model.ApprovalDecision;
import com.leave.management.approval.model.ApprovalLevel;
import java.time.LocalDateTime;

public class ApprovalStepDto {
    private Long id;
    private ApprovalLevel level;
    private Long approverId;
    private String approverName;
    private ApprovalDecision decision;
    private String comment;
    private LocalDateTime actedAt;

    public ApprovalStepDto() {}

    public ApprovalStepDto(Long id, ApprovalLevel level, Long approverId, String approverName,
                           ApprovalDecision decision, String comment, LocalDateTime actedAt) {
        this.id = id;
        this.level = level;
        this.approverId = approverId;
        this.approverName = approverName;
        this.decision = decision;
        this.comment = comment;
        this.actedAt = actedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private ApprovalLevel level;
        private Long approverId;
        private String approverName;
        private ApprovalDecision decision;
        private String comment;
        private LocalDateTime actedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder level(ApprovalLevel level) { this.level = level; return this; }
        public Builder approverId(Long approverId) { this.approverId = approverId; return this; }
        public Builder approverName(String approverName) { this.approverName = approverName; return this; }
        public Builder decision(ApprovalDecision decision) { this.decision = decision; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }
        public Builder actedAt(LocalDateTime actedAt) { this.actedAt = actedAt; return this; }

        public ApprovalStepDto build() {
            return new ApprovalStepDto(id, level, approverId, approverName, decision, comment, actedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ApprovalLevel getLevel() { return level; }
    public void setLevel(ApprovalLevel level) { this.level = level; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public ApprovalDecision getDecision() { return decision; }
    public void setDecision(ApprovalDecision decision) { this.decision = decision; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getActedAt() { return actedAt; }
    public void setActedAt(LocalDateTime actedAt) { this.actedAt = actedAt; }
}

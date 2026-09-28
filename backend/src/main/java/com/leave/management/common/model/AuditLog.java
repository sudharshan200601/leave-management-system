package com.leave.management.common.model;

import com.leave.management.leave.model.LeaveStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    private LeaveStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private LeaveStatus toStatus;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(Long id, String entityType, Long entityId, String action, LeaveStatus fromStatus, LeaveStatus toStatus, String performedBy, LocalDateTime timestamp) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String entityType;
        private Long entityId;
        private String action;
        private LeaveStatus fromStatus;
        private LeaveStatus toStatus;
        private String performedBy;
        private LocalDateTime timestamp;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(Long entityId) { this.entityId = entityId; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder fromStatus(LeaveStatus fromStatus) { this.fromStatus = fromStatus; return this; }
        public Builder toStatus(LeaveStatus toStatus) { this.toStatus = toStatus; return this; }
        public Builder performedBy(String performedBy) { this.performedBy = performedBy; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AuditLog build() {
            return new AuditLog(id, entityType, entityId, action, fromStatus, toStatus, performedBy, timestamp);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public LeaveStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(LeaveStatus fromStatus) { this.fromStatus = fromStatus; }
    public LeaveStatus getToStatus() { return toStatus; }
    public void setToStatus(LeaveStatus toStatus) { this.toStatus = toStatus; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

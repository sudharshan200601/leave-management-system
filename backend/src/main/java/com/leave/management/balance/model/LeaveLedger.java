package com.leave.management.balance.model;

import com.leave.management.employee.model.Employee;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_ledgers")
public class LeaveLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "ledger_year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryType entryType;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal days;

    @Column(name = "leave_request_id")
    private Long leaveRequestId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public LeaveLedger() {}

    public LeaveLedger(Long id, Employee employee, Integer year, LedgerEntryType entryType, BigDecimal days, Long leaveRequestId, LocalDateTime createdAt) {
        this.id = id;
        this.employee = employee;
        this.year = year;
        this.entryType = entryType;
        this.days = days;
        this.leaveRequestId = leaveRequestId;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Employee employee;
        private Integer year;
        private LedgerEntryType entryType;
        private BigDecimal days;
        private Long leaveRequestId;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder employee(Employee employee) { this.employee = employee; return this; }
        public Builder year(Integer year) { this.year = year; return this; }
        public Builder entryType(LedgerEntryType entryType) { this.entryType = entryType; return this; }
        public Builder days(BigDecimal days) { this.days = days; return this; }
        public Builder leaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LeaveLedger build() {
            return new LeaveLedger(id, employee, year, entryType, days, leaveRequestId, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public LedgerEntryType getEntryType() { return entryType; }
    public void setEntryType(LedgerEntryType entryType) { this.entryType = entryType; }
    public BigDecimal getDays() { return days; }
    public void setDays(BigDecimal days) { this.days = days; }
    public Long getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.leave.management.balance.dto;

import java.math.BigDecimal;

public class BalanceResponseDto {
    private Long employeeId;
    private String employeeName;
    private Integer year;
    private BigDecimal entitlement;
    private BigDecimal reserved;
    private BigDecimal used;
    private BigDecimal available;

    public BalanceResponseDto() {}

    public BalanceResponseDto(Long employeeId, String employeeName, Integer year, BigDecimal entitlement, BigDecimal reserved, BigDecimal used, BigDecimal available) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.year = year;
        this.entitlement = entitlement;
        this.reserved = reserved;
        this.used = used;
        this.available = available;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long employeeId;
        private String employeeName;
        private Integer year;
        private BigDecimal entitlement;
        private BigDecimal reserved;
        private BigDecimal used;
        private BigDecimal available;

        public Builder employeeId(Long employeeId) { this.employeeId = employeeId; return this; }
        public Builder employeeName(String employeeName) { this.employeeName = employeeName; return this; }
        public Builder year(Integer year) { this.year = year; return this; }
        public Builder entitlement(BigDecimal entitlement) { this.entitlement = entitlement; return this; }
        public Builder reserved(BigDecimal reserved) { this.reserved = reserved; return this; }
        public Builder used(BigDecimal used) { this.used = used; return this; }
        public Builder available(BigDecimal available) { this.available = available; return this; }

        public BalanceResponseDto build() {
            return new BalanceResponseDto(employeeId, employeeName, year, entitlement, reserved, used, available);
        }
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BigDecimal getEntitlement() { return entitlement; }
    public void setEntitlement(BigDecimal entitlement) { this.entitlement = entitlement; }
    public BigDecimal getReserved() { return reserved; }
    public void setReserved(BigDecimal reserved) { this.reserved = reserved; }
    public BigDecimal getUsed() { return used; }
    public void setUsed(BigDecimal used) { this.used = used; }
    public BigDecimal getAvailable() { return available; }
    public void setAvailable(BigDecimal available) { this.available = available; }
}

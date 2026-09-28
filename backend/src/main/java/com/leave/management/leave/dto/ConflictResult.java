package com.leave.management.leave.dto;

import com.leave.management.leave.model.ConflictSeverity;
import java.time.LocalDate;

public class ConflictResult {
    private boolean conflictFlag;
    private ConflictSeverity severity;
    private LocalDate peakDate;
    private int peakAbsentCount;
    private long teamSize;
    private double peakPercentage;
    private String details;

    public ConflictResult() {}

    public ConflictResult(boolean conflictFlag, ConflictSeverity severity, LocalDate peakDate, int peakAbsentCount,
                          long teamSize, double peakPercentage, String details) {
        this.conflictFlag = conflictFlag;
        this.severity = severity;
        this.peakDate = peakDate;
        this.peakAbsentCount = peakAbsentCount;
        this.teamSize = teamSize;
        this.peakPercentage = peakPercentage;
        this.details = details;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean conflictFlag;
        private ConflictSeverity severity;
        private LocalDate peakDate;
        private int peakAbsentCount;
        private long teamSize;
        private double peakPercentage;
        private String details;

        public Builder conflictFlag(boolean conflictFlag) { this.conflictFlag = conflictFlag; return this; }
        public Builder severity(ConflictSeverity severity) { this.severity = severity; return this; }
        public Builder peakDate(LocalDate peakDate) { this.peakDate = peakDate; return this; }
        public Builder peakAbsentCount(int peakAbsentCount) { this.peakAbsentCount = peakAbsentCount; return this; }
        public Builder teamSize(long teamSize) { this.teamSize = teamSize; return this; }
        public Builder peakPercentage(double peakPercentage) { this.peakPercentage = peakPercentage; return this; }
        public Builder details(String details) { this.details = details; return this; }

        public ConflictResult build() {
            return new ConflictResult(conflictFlag, severity, peakDate, peakAbsentCount, teamSize, peakPercentage, details);
        }
    }

    public boolean isConflictFlag() { return conflictFlag; }
    public void setConflictFlag(boolean conflictFlag) { this.conflictFlag = conflictFlag; }
    public ConflictSeverity getSeverity() { return severity; }
    public void setSeverity(ConflictSeverity severity) { this.severity = severity; }
    public LocalDate getPeakDate() { return peakDate; }
    public void setPeakDate(LocalDate peakDate) { this.peakDate = peakDate; }
    public int getPeakAbsentCount() { return peakAbsentCount; }
    public void setPeakAbsentCount(int peakAbsentCount) { this.peakAbsentCount = peakAbsentCount; }
    public long getTeamSize() { return teamSize; }
    public void setTeamSize(long teamSize) { this.teamSize = teamSize; }
    public double getPeakPercentage() { return peakPercentage; }
    public void setPeakPercentage(double peakPercentage) { this.peakPercentage = peakPercentage; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

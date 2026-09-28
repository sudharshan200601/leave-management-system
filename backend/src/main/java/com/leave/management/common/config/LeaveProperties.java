package com.leave.management.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "leave")
public class LeaveProperties {

    private Escalation escalation = new Escalation();
    private Conflict conflict = new Conflict();
    private BigDecimal annualQuotaDays = new BigDecimal("24");
    private Proration proration = new Proration();

    public Escalation getEscalation() { return escalation; }
    public void setEscalation(Escalation escalation) { this.escalation = escalation; }
    public Conflict getConflict() { return conflict; }
    public void setConflict(Conflict conflict) { this.conflict = conflict; }
    public BigDecimal getAnnualQuotaDays() { return annualQuotaDays; }
    public void setAnnualQuotaDays(BigDecimal annualQuotaDays) { this.annualQuotaDays = annualQuotaDays; }
    public Proration getProration() { return proration; }
    public void setProration(Proration proration) { this.proration = proration; }

    public static class Escalation {
        private int managerTimeoutHours = 24;
        private int hrTimeoutHours = 72;
        private int checkIntervalMinutes = 15;

        public int getManagerTimeoutHours() { return managerTimeoutHours; }
        public void setManagerTimeoutHours(int managerTimeoutHours) { this.managerTimeoutHours = managerTimeoutHours; }
        public int getHrTimeoutHours() { return hrTimeoutHours; }
        public void setHrTimeoutHours(int hrTimeoutHours) { this.hrTimeoutHours = hrTimeoutHours; }
        public int getCheckIntervalMinutes() { return checkIntervalMinutes; }
        public void setCheckIntervalMinutes(int checkIntervalMinutes) { this.checkIntervalMinutes = checkIntervalMinutes; }
    }

    public static class Conflict {
        private double maxTeamAbsencePercent = 40.0;

        public double getMaxTeamAbsencePercent() { return maxTeamAbsencePercent; }
        public void setMaxTeamAbsencePercent(double maxTeamAbsencePercent) { this.maxTeamAbsencePercent = maxTeamAbsencePercent; }
    }

    public static class Proration {
        private String method = "MONTHLY";

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
    }
}

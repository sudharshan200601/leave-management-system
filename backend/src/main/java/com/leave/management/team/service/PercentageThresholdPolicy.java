package com.leave.management.team.service;

import com.leave.management.common.config.LeaveProperties;
import com.leave.management.employee.repository.EmployeeRepository;
import com.leave.management.leave.dto.ConflictResult;
import com.leave.management.leave.model.ConflictSeverity;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.repository.LeaveRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PercentageThresholdPolicy implements ConflictPolicy {

    private static final Logger log = LoggerFactory.getLogger(PercentageThresholdPolicy.class);

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveProperties leaveProperties;

    public PercentageThresholdPolicy(EmployeeRepository employeeRepository,
                                     LeaveRequestRepository leaveRequestRepository,
                                     LeaveProperties leaveProperties) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveProperties = leaveProperties;
    }

    @Override
    public ConflictResult evaluate(LeaveRequest candidate) {
        Long teamId = candidate.getEmployee().getTeamId();
        long teamSize = employeeRepository.countByTeamId(teamId);
        if (teamSize == 0) {
            teamSize = 1;
        }

        List<LeaveStatus> activeStatuses = List.of(
                LeaveStatus.PENDING_MANAGER,
                LeaveStatus.PENDING_HR,
                LeaveStatus.APPROVED
        );

        List<LeaveRequest> teamRequests = leaveRequestRepository.findOverlappingTeamRequests(
                teamId, activeStatuses, candidate.getStartDate(), candidate.getEndDate());

        LocalDate peakDate = candidate.getStartDate();
        int peakAbsent = 1;

        for (LocalDate d = candidate.getStartDate(); !d.isAfter(candidate.getEndDate()); d = d.plusDays(1)) {
            final LocalDate current = d;
            long teammateAbsent = teamRequests.stream()
                    .filter(r -> candidate.getId() == null || !r.getId().equals(candidate.getId()))
                    .filter(r -> !r.getEmployee().getId().equals(candidate.getEmployee().getId()))
                    .filter(r -> !r.getStartDate().isAfter(current) && !r.getEndDate().isBefore(current))
                    .map(r -> r.getEmployee().getId())
                    .distinct()
                    .count();

            int totalAbsent = 1 + (int) teammateAbsent;
            if (totalAbsent > peakAbsent) {
                peakAbsent = totalAbsent;
                peakDate = current;
            }
        }

        double peakPercentage = (peakAbsent * 100.0) / teamSize;
        double maxThreshold = leaveProperties.getConflict().getMaxTeamAbsencePercent();
        double lowThreshold = maxThreshold * 0.75;

        ConflictSeverity severity;
        boolean flag;

        if (peakAbsent <= 1) {
            severity = ConflictSeverity.NONE;
            flag = false;
        } else if (peakPercentage > maxThreshold) {
            severity = ConflictSeverity.HIGH;
            flag = true;
        } else if (peakPercentage >= lowThreshold) {
            severity = ConflictSeverity.LOW;
            flag = true;
        } else {
            severity = ConflictSeverity.NONE;
            flag = false;
        }

        String details = flag
                ? String.format("%d of %d team members absent on %s (Overlap detected)", peakAbsent, teamSize, peakDate)
                : "No team overlap detected";

        return ConflictResult.builder()
                .conflictFlag(flag)
                .severity(severity)
                .peakDate(peakDate)
                .peakAbsentCount(peakAbsent)
                .teamSize(teamSize)
                .peakPercentage(peakPercentage)
                .details(details)
                .build();
    }
}

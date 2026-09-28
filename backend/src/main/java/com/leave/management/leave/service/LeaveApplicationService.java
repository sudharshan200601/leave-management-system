package com.leave.management.leave.service;

import com.leave.management.balance.dto.BalanceResponseDto;
import com.leave.management.balance.service.BalanceService;
import com.leave.management.common.exception.InsufficientBalanceException;
import com.leave.management.common.exception.InvalidLeaveRequestException;
import com.leave.management.common.model.AuditLog;
import com.leave.management.common.repository.AuditLogRepository;
import com.leave.management.employee.model.Employee;
import com.leave.management.leave.dto.ConflictResult;
import com.leave.management.leave.dto.CreateLeaveRequestDto;
import com.leave.management.leave.model.ConflictSeverity;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.repository.LeaveRequestRepository;
import com.leave.management.team.service.ConflictPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveApplicationService.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final WorkingDaysCalculator workingDaysCalculator;
    private final BalanceService balanceService;
    private final ConflictPolicy conflictPolicy;
    private final AuditLogRepository auditLogRepository;

    public LeaveApplicationService(LeaveRequestRepository leaveRequestRepository,
                                   WorkingDaysCalculator workingDaysCalculator,
                                   BalanceService balanceService,
                                   ConflictPolicy conflictPolicy,
                                   AuditLogRepository auditLogRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.workingDaysCalculator = workingDaysCalculator;
        this.balanceService = balanceService;
        this.conflictPolicy = conflictPolicy;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public LeaveRequest applyForLeave(Employee employee, CreateLeaveRequestDto dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();

        if (startDate == null || endDate == null) {
            throw new InvalidLeaveRequestException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new InvalidLeaveRequestException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidLeaveRequestException("Start date cannot be in the past");
        }

        List<LeaveStatus> activeStatuses = List.of(
                LeaveStatus.PENDING_MANAGER,
                LeaveStatus.PENDING_HR,
                LeaveStatus.APPROVED
        );

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingEmployeeRequests(
                employee.getId(), activeStatuses, startDate, endDate);

        if (!overlapping.isEmpty()) {
            throw new InvalidLeaveRequestException("You already have an active leave request overlapping with this date range");
        }

        BigDecimal workingDays = workingDaysCalculator.calculateWorkingDays(startDate, endDate);
        if (workingDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLeaveRequestException("Selected date range contains 0 working days (weekends or holidays)");
        }

        if (dto.getLeaveType() == com.leave.management.leave.model.LeaveType.COMP_OFF) {
            if (dto.getCompensatoryWorkingDate() == null) {
                throw new InvalidLeaveRequestException("Compensatory working weekend date is required for Comp-off");
            }
            java.time.DayOfWeek day = dto.getCompensatoryWorkingDate().getDayOfWeek();
            if (day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY) {
                throw new InvalidLeaveRequestException("Compensatory working date must be a weekend (Saturday or Sunday)");
            }
        }

        if (dto.getLeaveType() == com.leave.management.leave.model.LeaveType.ON_DUTY) {
            if (dto.getOdType() == null || dto.getOdType().isBlank() ||
                dto.getLocation() == null || dto.getLocation().isBlank() ||
                dto.getStartTime() == null || dto.getStartTime().isBlank() ||
                dto.getEndTime() == null || dto.getEndTime().isBlank() ||
                dto.getReason() == null || dto.getReason().isBlank()) {
                throw new InvalidLeaveRequestException("OD Type, Location, Start Time, End Time, and Purpose are required for OD requests");
            }
        }

        if (dto.getDocumentData() != null && !dto.getDocumentData().isBlank()) {
            String base64Content = dto.getDocumentData();
            if (base64Content.contains(",")) {
                base64Content = base64Content.substring(base64Content.indexOf(",") + 1);
            }
            int decodedByteSize = (int) (base64Content.length() * 0.75);
            if (decodedByteSize > 102400) {
                throw new InvalidLeaveRequestException(String.format("Attached document exceeds 100 KB limit (Size: %d KB)", decodedByteSize / 1024));
            }
        }

        if (dto.getLeaveType() == com.leave.management.leave.model.LeaveType.ANNUAL) {
            BalanceResponseDto balance = balanceService.getBalance(employee, startDate.getYear());
            if (balance.getAvailable().compareTo(workingDays) < 0) {
                throw new InsufficientBalanceException(String.format(
                        "Insufficient leave balance. Required: %s days, Available: %s days", workingDays, balance.getAvailable()));
            }
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(dto.getLeaveType())
                .startDate(startDate)
                .endDate(endDate)
                .workingDays(workingDays)
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING_MANAGER)
                .conflictFlag(false)
                .conflictSeverity(ConflictSeverity.NONE)
                .submittedAt(LocalDateTime.now())
                .lastActionAt(LocalDateTime.now())
                .escalated(false)
                .compensatoryWorkingDate(dto.getCompensatoryWorkingDate())
                .odType(dto.getOdType())
                .location(dto.getLocation())
                .remarks(dto.getRemarks())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .documentName(dto.getDocumentName())
                .documentData(dto.getDocumentData())
                .build();

        ConflictResult conflictResult = conflictPolicy.evaluate(request);
        request.setConflictFlag(conflictResult.isConflictFlag());
        request.setConflictSeverity(conflictResult.getSeverity());
        request.setConflictDetails(conflictResult.getDetails());

        LeaveRequest savedRequest = leaveRequestRepository.save(request);

        if (savedRequest.getLeaveType() == com.leave.management.leave.model.LeaveType.ANNUAL) {
            balanceService.recordReserve(employee, startDate.getYear(), workingDays, savedRequest.getId());
        }

        AuditLog auditLog = AuditLog.builder()
                .entityType("LeaveRequest")
                .entityId(savedRequest.getId())
                .action("SUBMIT")
                .fromStatus(null)
                .toStatus(LeaveStatus.PENDING_MANAGER)
                .performedBy(employee.getEmail())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);

        log.info("Successfully submitted leave request #{} for employee {} (Working days: {})",
                savedRequest.getId(), employee.getEmail(), workingDays);

        return savedRequest;
    }

    @Transactional
    public void reevaluateTeamConflicts(Long teamId, LocalDate startDate, LocalDate endDate) {
        List<LeaveStatus> activeStatuses = List.of(
                LeaveStatus.PENDING_MANAGER,
                LeaveStatus.PENDING_HR,
                LeaveStatus.APPROVED
        );

        List<LeaveRequest> teamRequests = leaveRequestRepository.findOverlappingTeamRequests(
                teamId, activeStatuses, startDate, endDate);

        for (LeaveRequest req : teamRequests) {
            ConflictResult result = conflictPolicy.evaluate(req);
            req.setConflictFlag(result.isConflictFlag());
            req.setConflictSeverity(result.getSeverity());
            req.setConflictDetails(result.getDetails());
            leaveRequestRepository.save(req);
        }
    }
}

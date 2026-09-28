package com.leave.management.swap.service;

import com.leave.management.balance.service.BalanceService;
import com.leave.management.balance.repository.LeaveLedgerRepository;
import com.leave.management.balance.model.LeaveLedger;
import com.leave.management.balance.model.LedgerEntryType;
import com.leave.management.common.exception.InvalidLeaveRequestException;
import com.leave.management.common.exception.ResourceNotFoundException;
import com.leave.management.common.exception.UnauthorizedAccessException;
import com.leave.management.common.model.AuditLog;
import com.leave.management.common.repository.AuditLogRepository;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.model.Role;
import com.leave.management.employee.repository.EmployeeRepository;
import com.leave.management.leave.model.ConflictSeverity;
import com.leave.management.leave.dto.ConflictResult;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.model.LeaveType;
import com.leave.management.leave.repository.LeaveRequestRepository;
import com.leave.management.leave.service.LeaveApplicationService;
import com.leave.management.leave.service.WorkingDaysCalculator;
import com.leave.management.swap.dto.CreateSwapRequestDto;
import com.leave.management.swap.dto.EligibleSwapDto;
import com.leave.management.swap.dto.SwapResponseDto;
import com.leave.management.swap.model.LeaveSwapRequest;
import com.leave.management.swap.model.SwapStatus;
import com.leave.management.swap.repository.LeaveSwapRequestRepository;
import com.leave.management.team.service.ConflictPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveSwapService {

    private final LeaveSwapRequestRepository swapRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final BalanceService balanceService;
    private final WorkingDaysCalculator workingDaysCalculator;
    private final ConflictPolicy conflictPolicy;
    private final AuditLogRepository auditLogRepository;
    private final LeaveLedgerRepository ledgerRepository;

    public LeaveSwapService(LeaveSwapRequestRepository swapRepository,
                            LeaveRequestRepository leaveRequestRepository,
                            EmployeeRepository employeeRepository,
                            BalanceService balanceService,
                            WorkingDaysCalculator workingDaysCalculator,
                            ConflictPolicy conflictPolicy,
                            AuditLogRepository auditLogRepository,
                            LeaveLedgerRepository ledgerRepository) {
        this.swapRepository = swapRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.balanceService = balanceService;
        this.workingDaysCalculator = workingDaysCalculator;
        this.conflictPolicy = conflictPolicy;
        this.auditLogRepository = auditLogRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public List<EligibleSwapDto> getEligibleSwaps(Employee requester, Long leaveRequestId) {
        LeaveRequest req = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        if (!req.getEmployee().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("Can only find swaps for your own leave requests");
        }

        List<LeaveStatus> statuses = List.of(LeaveStatus.PENDING_MANAGER, LeaveStatus.PENDING_HR, LeaveStatus.APPROVED);
        List<LeaveRequest> teamLeaves = leaveRequestRepository.findOverlappingTeamRequests(
                requester.getTeamId(), statuses, req.getStartDate(), req.getEndDate());

        return teamLeaves.stream()
                .filter(l -> !l.getEmployee().getId().equals(requester.getId()))
                .filter(l -> l.getLeaveType() == LeaveType.ANNUAL) // Only swap annual leave for simplicity
                .map(l -> new EligibleSwapDto(
                        l.getEmployee().getId(),
                        l.getEmployee().getName(),
                        l.getId(),
                        l.getStartDate(),
                        l.getEndDate()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public SwapResponseDto proposeSwap(Employee requester, CreateSwapRequestDto dto) {
        LeaveRequest requesterReq = leaveRequestRepository.findById(dto.getRequesterLeaveRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Requester leave request not found"));
        
        LeaveRequest targetReq = leaveRequestRepository.findById(dto.getTargetLeaveRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Target leave request not found"));

        if (!targetReq.getEmployee().getTeamId().equals(requester.getTeamId())) {
            throw new InvalidLeaveRequestException("Target employee must be on the same team");
        }

        if (dto.getTargetNewStartDate().isAfter(dto.getTargetNewEndDate()) || dto.getTargetNewStartDate().isBefore(LocalDate.now())) {
            throw new InvalidLeaveRequestException("Invalid new dates for target employee");
        }

        BigDecimal newWorkingDays = workingDaysCalculator.calculateWorkingDays(dto.getTargetNewStartDate(), dto.getTargetNewEndDate());
        if (newWorkingDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLeaveRequestException("Target new dates contain 0 working days");
        }

        LeaveSwapRequest swap = new LeaveSwapRequest();
        swap.setRequester(requester);
        swap.setTargetEmployee(targetReq.getEmployee());
        swap.setRequesterLeaveRequest(requesterReq);
        swap.setTargetLeaveRequest(targetReq);
        swap.setTargetNewStartDate(dto.getTargetNewStartDate());
        swap.setTargetNewEndDate(dto.getTargetNewEndDate());
        swap.setStatus(SwapStatus.PENDING_TARGET);
        swap.setCreatedAt(LocalDateTime.now());
        swap.setUpdatedAt(LocalDateTime.now());

        LeaveSwapRequest saved = swapRepository.save(swap);

        AuditLog log = AuditLog.builder()
                .entityType("LeaveSwapRequest")
                .entityId(saved.getId())
                .action("PROPOSE")
                .toStatus(LeaveStatus.valueOf(SwapStatus.PENDING_TARGET.name()))
                .performedBy(requester.getEmail())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);

        return mapToDto(saved);
    }

    @Transactional
    public SwapResponseDto acceptSwap(Long swapId, Employee target) {
        LeaveSwapRequest swap = swapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));

        if (!swap.getTargetEmployee().getId().equals(target.getId())) {
            throw new UnauthorizedAccessException("Only the target can accept the swap");
        }
        if (swap.getStatus() != SwapStatus.PENDING_TARGET) {
            throw new InvalidLeaveRequestException("Swap is not pending target acceptance");
        }

        swap.setStatus(SwapStatus.PENDING_MANAGER);
        swap.setUpdatedAt(LocalDateTime.now());
        LeaveSwapRequest saved = swapRepository.save(swap);

        AuditLog log = AuditLog.builder()
                .entityType("LeaveSwapRequest")
                .entityId(saved.getId())
                .action("ACCEPT")
                .toStatus(LeaveStatus.valueOf(SwapStatus.PENDING_MANAGER.name()))
                .performedBy(target.getEmail())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);

        return mapToDto(saved);
    }

    @Transactional
    public SwapResponseDto declineSwap(Long swapId, Employee target) {
        LeaveSwapRequest swap = swapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));

        if (!swap.getTargetEmployee().getId().equals(target.getId())) {
            throw new UnauthorizedAccessException("Only the target can decline the swap");
        }
        if (swap.getStatus() != SwapStatus.PENDING_TARGET) {
            throw new InvalidLeaveRequestException("Swap is not pending target acceptance");
        }

        swap.setStatus(SwapStatus.REJECTED);
        swap.setUpdatedAt(LocalDateTime.now());
        LeaveSwapRequest saved = swapRepository.save(swap);

        AuditLog log = AuditLog.builder()
                .entityType("LeaveSwapRequest")
                .entityId(saved.getId())
                .action("DECLINE")
                .toStatus(LeaveStatus.valueOf(SwapStatus.REJECTED.name()))
                .performedBy(target.getEmail())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);

        return mapToDto(saved);
    }

    @Transactional
    public SwapResponseDto approveSwap(Long swapId, Employee manager) {
        LeaveSwapRequest swap = swapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));

        if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.HR) {
            throw new UnauthorizedAccessException("Only manager or HR can approve a swap");
        }
        if (swap.getStatus() != SwapStatus.PENDING_MANAGER) {
            throw new InvalidLeaveRequestException("Swap is not pending manager approval");
        }

        LeaveRequest oldRequesterReq = swap.getRequesterLeaveRequest();
        LeaveRequest oldTargetReq = swap.getTargetLeaveRequest();

        LeaveStatus prevTargetStatus = oldTargetReq.getStatus();

        // Target old request cancelled
        oldTargetReq.setStatus(LeaveStatus.CANCELLED);
        oldTargetReq.setLastActionAt(LocalDateTime.now());
        leaveRequestRepository.save(oldTargetReq);

        // Refund Target's old leave
        if (oldTargetReq.getLeaveType() == LeaveType.ANNUAL) {
            balanceService.recordRelease(oldTargetReq.getEmployee(), oldTargetReq.getStartDate().getYear(), oldTargetReq.getWorkingDays(), oldTargetReq.getId());
            // If it was already approved, add CREDIT to undo DEBIT
            if (prevTargetStatus == LeaveStatus.APPROVED) {
                LeaveLedger credit = LeaveLedger.builder()
                    .employee(oldTargetReq.getEmployee())
                    .year(oldTargetReq.getStartDate().getYear())
                    .entryType(LedgerEntryType.CREDIT)
                    .days(oldTargetReq.getWorkingDays())
                    .leaveRequestId(oldTargetReq.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
                ledgerRepository.save(credit);
            }
        }

        // Requester old request is approved
        oldRequesterReq.setConflictFlag(false);
        oldRequesterReq.setConflictSeverity(ConflictSeverity.NONE);
        oldRequesterReq.setConflictDetails("Resolved via swap");
        oldRequesterReq.setStatus(LeaveStatus.APPROVED);
        oldRequesterReq.setLastActionAt(LocalDateTime.now());
        leaveRequestRepository.save(oldRequesterReq);

        // Create new leave for target
        BigDecimal newWorkingDays = workingDaysCalculator.calculateWorkingDays(swap.getTargetNewStartDate(), swap.getTargetNewEndDate());
        LeaveRequest newTargetReq = LeaveRequest.builder()
                .employee(swap.getTargetEmployee())
                .leaveType(LeaveType.ANNUAL)
                .startDate(swap.getTargetNewStartDate())
                .endDate(swap.getTargetNewEndDate())
                .workingDays(newWorkingDays)
                .reason("Swapped with " + swap.getRequester().getName())
                .status(LeaveStatus.APPROVED)
                .conflictFlag(false)
                .conflictSeverity(ConflictSeverity.NONE)
                .conflictDetails("Approved via swap")
                .submittedAt(LocalDateTime.now())
                .lastActionAt(LocalDateTime.now())
                .escalated(false)
                .build();
        newTargetReq = leaveRequestRepository.save(newTargetReq);
        balanceService.recordApproval(swap.getTargetEmployee(), swap.getTargetNewStartDate().getYear(), newWorkingDays, newTargetReq.getId());
        
        swap.setStatus(SwapStatus.APPROVED);
        swap.setUpdatedAt(LocalDateTime.now());
        LeaveSwapRequest saved = swapRepository.save(swap);

        AuditLog log = AuditLog.builder()
                .entityType("LeaveSwapRequest")
                .entityId(saved.getId())
                .action("MANAGER_APPROVE")
                .toStatus(LeaveStatus.valueOf(SwapStatus.APPROVED.name()))
                .performedBy(manager.getEmail())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);

        return mapToDto(saved);
    }

    public List<SwapResponseDto> getPendingSwapsForManager(Long managerId) {
        return swapRepository.findByStatus(SwapStatus.PENDING_MANAGER).stream()
                .filter(s -> s.getRequester().getManager() != null && s.getRequester().getManager().getId().equals(managerId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public List<SwapResponseDto> getSwapsForEmployee(Long employeeId) {
        List<SwapResponseDto> list1 = swapRepository.findByRequesterId(employeeId).stream().map(this::mapToDto).collect(Collectors.toList());
        List<SwapResponseDto> list2 = swapRepository.findByTargetEmployeeId(employeeId).stream().map(this::mapToDto).collect(Collectors.toList());
        list1.addAll(list2);
        return list1;
    }

    private SwapResponseDto mapToDto(LeaveSwapRequest swap) {
        SwapResponseDto dto = new SwapResponseDto();
        dto.setId(swap.getId());
        dto.setRequesterId(swap.getRequester().getId());
        dto.setRequesterName(swap.getRequester().getName());
        dto.setTargetEmployeeId(swap.getTargetEmployee().getId());
        dto.setTargetEmployeeName(swap.getTargetEmployee().getName());
        dto.setRequesterLeaveRequestId(swap.getRequesterLeaveRequest().getId());
        dto.setTargetLeaveRequestId(swap.getTargetLeaveRequest().getId());
        dto.setOriginalStartDate(swap.getRequesterLeaveRequest().getStartDate());
        dto.setOriginalEndDate(swap.getRequesterLeaveRequest().getEndDate());
        dto.setTargetNewStartDate(swap.getTargetNewStartDate());
        dto.setTargetNewEndDate(swap.getTargetNewEndDate());
        dto.setStatus(swap.getStatus());
        dto.setCreatedAt(swap.getCreatedAt());
        dto.setUpdatedAt(swap.getUpdatedAt());

        // Coverage simulation
        ConflictResult before = conflictPolicy.evaluate(swap.getRequesterLeaveRequest());
        dto.setCoverageBefore(before.getSeverity() == ConflictSeverity.HIGH ? "Critical (Overload)" : "Normal");
        
        LeaveRequest tempReq = LeaveRequest.builder()
            .employee(swap.getTargetEmployee())
            .startDate(swap.getTargetNewStartDate())
            .endDate(swap.getTargetNewEndDate())
            .build();
        ConflictResult after = conflictPolicy.evaluate(tempReq); // rudimentary simulation
        dto.setCoverageAfter(after.getSeverity() == ConflictSeverity.HIGH ? "Critical on Target's New Dates" : "Resolved");

        return dto;
    }
}

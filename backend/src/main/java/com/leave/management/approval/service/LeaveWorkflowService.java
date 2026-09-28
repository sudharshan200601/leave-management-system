package com.leave.management.approval.service;

import com.leave.management.approval.model.ApprovalDecision;
import com.leave.management.approval.model.ApprovalLevel;
import com.leave.management.approval.model.ApprovalStep;
import com.leave.management.approval.repository.ApprovalStepRepository;
import com.leave.management.balance.service.BalanceService;
import com.leave.management.common.exception.IllegalStateTransitionException;
import com.leave.management.common.exception.ResourceNotFoundException;
import com.leave.management.common.exception.UnauthorizedAccessException;
import com.leave.management.common.model.AuditLog;
import com.leave.management.common.repository.AuditLogRepository;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.model.Role;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.repository.LeaveRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
public class LeaveWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(LeaveWorkflowService.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final AuditLogRepository auditLogRepository;
    private final BalanceService balanceService;

    public LeaveWorkflowService(LeaveRequestRepository leaveRequestRepository,
                                ApprovalStepRepository approvalStepRepository,
                                AuditLogRepository auditLogRepository,
                                BalanceService balanceService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.auditLogRepository = auditLogRepository;
        this.balanceService = balanceService;
    }

    private static final Map<LeaveStatus, Map<LeaveAction, LeaveStatus>> TRANSITION_MATRIX = new EnumMap<>(LeaveStatus.class);

    static {
        Map<LeaveAction, LeaveStatus> fromPendingManager = new EnumMap<>(LeaveAction.class);
        fromPendingManager.put(LeaveAction.MANAGER_APPROVE, LeaveStatus.PENDING_HR);
        fromPendingManager.put(LeaveAction.MANAGER_REJECT, LeaveStatus.REJECTED);
        fromPendingManager.put(LeaveAction.CANCEL, LeaveStatus.CANCELLED);
        fromPendingManager.put(LeaveAction.ESCALATE, LeaveStatus.PENDING_HR);
        TRANSITION_MATRIX.put(LeaveStatus.PENDING_MANAGER, fromPendingManager);

        Map<LeaveAction, LeaveStatus> fromPendingHr = new EnumMap<>(LeaveAction.class);
        fromPendingHr.put(LeaveAction.HR_APPROVE, LeaveStatus.APPROVED);
        fromPendingHr.put(LeaveAction.HR_REJECT, LeaveStatus.REJECTED);
        fromPendingHr.put(LeaveAction.CANCEL, LeaveStatus.CANCELLED);
        TRANSITION_MATRIX.put(LeaveStatus.PENDING_HR, fromPendingHr);

        Map<LeaveAction, LeaveStatus> fromApproved = new EnumMap<>(LeaveAction.class);
        fromApproved.put(LeaveAction.CANCEL, LeaveStatus.CANCELLED);
        TRANSITION_MATRIX.put(LeaveStatus.APPROVED, fromApproved);
    }

    @Transactional
    public LeaveRequest transition(Long requestId, LeaveAction action, Employee actor, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + requestId));

        LeaveStatus currentStatus = request.getStatus();

        Map<LeaveAction, LeaveStatus> allowedActions = TRANSITION_MATRIX.get(currentStatus);
        if (allowedActions == null || !allowedActions.containsKey(action)) {
            throw new IllegalStateTransitionException(
                    "Invalid state transition: Cannot perform action " + action + " on request in status " + currentStatus);
        }

        LeaveStatus targetStatus = allowedActions.get(action);

        if (currentStatus == LeaveStatus.APPROVED && action == LeaveAction.CANCEL) {
            if (!request.getStartDate().isAfter(LocalDate.now())) {
                throw new IllegalStateTransitionException(
                        "Cannot cancel an approved leave request whose start date (" + request.getStartDate() + ") is today or in the past");
            }
        }

        verifyAuthority(request, action, actor);

        request.setStatus(targetStatus);
        request.setLastActionAt(LocalDateTime.now());
        if (action == LeaveAction.ESCALATE) {
            request.setEscalated(true);
        }

        LeaveRequest savedRequest = leaveRequestRepository.save(request);

        ApprovalLevel level = (action == LeaveAction.HR_APPROVE || action == LeaveAction.HR_REJECT || currentStatus == LeaveStatus.PENDING_HR)
                ? ApprovalLevel.HR : ApprovalLevel.MANAGER;

        ApprovalDecision decision = mapActionToDecision(action);

        ApprovalStep step = ApprovalStep.builder()
                .leaveRequest(savedRequest)
                .level(level)
                .approver(actor)
                .decision(decision)
                .comment(comment != null ? comment : action.name())
                .actedAt(LocalDateTime.now())
                .build();
        approvalStepRepository.save(step);

        AuditLog auditLog = AuditLog.builder()
                .entityType("LeaveRequest")
                .entityId(savedRequest.getId())
                .action(action.name())
                .fromStatus(currentStatus)
                .toStatus(targetStatus)
                .performedBy(actor != null ? actor.getEmail() : "SYSTEM")
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);

        handleBalanceChanges(savedRequest, action, currentStatus);

        log.info("Transitioned LeaveRequest #{} from {} to {} via action {} by {}",
                savedRequest.getId(), currentStatus, targetStatus, action, actor != null ? actor.getEmail() : "SYSTEM");

        return savedRequest;
    }

    private void verifyAuthority(LeaveRequest request, LeaveAction action, Employee actor) {
        if (actor == null) {
            if (action == LeaveAction.ESCALATE) {
                return;
            }
            throw new UnauthorizedAccessException("Actor cannot be null for action: " + action);
        }

        Employee applicant = request.getEmployee();

        switch (action) {
            case MANAGER_APPROVE:
            case MANAGER_REJECT:
                if (actor.getId().equals(applicant.getId())) {
                    throw new UnauthorizedAccessException("Manager cannot approve or reject their own leave request");
                }
                if (actor.getRole() == Role.HR) {
                    return;
                }
                if (actor.getRole() != Role.MANAGER || applicant.getManager() == null || !applicant.getManager().getId().equals(actor.getId())) {
                    throw new UnauthorizedAccessException("Only the assigned manager or HR can act on this request");
                }
                break;

            case HR_APPROVE:
            case HR_REJECT:
                if (actor.getRole() != Role.HR) {
                    throw new UnauthorizedAccessException("Only HR can perform HR approval/rejection");
                }
                break;

            case CANCEL:
                if (!actor.getId().equals(applicant.getId()) && actor.getRole() != Role.HR) {
                    throw new UnauthorizedAccessException("You can only cancel your own leave request");
                }
                break;

            case ESCALATE:
                break;

            default:
                break;
        }
    }

    private ApprovalDecision mapActionToDecision(LeaveAction action) {
        switch (action) {
            case MANAGER_APPROVE:
            case HR_APPROVE:
                return ApprovalDecision.APPROVED;
            case MANAGER_REJECT:
            case HR_REJECT:
                return ApprovalDecision.REJECTED;
            case ESCALATE:
                return ApprovalDecision.ESCALATED;
            default:
                return ApprovalDecision.REJECTED;
        }
    }

    private void handleBalanceChanges(LeaveRequest request, LeaveAction action, LeaveStatus previousStatus) {
        if (request.getLeaveType() != null && request.getLeaveType() != com.leave.management.leave.model.LeaveType.ANNUAL) {
            return;
        }
        int year = request.getStartDate().getYear();
        Employee employee = request.getEmployee();

        if (action == LeaveAction.HR_APPROVE) {
            balanceService.recordApproval(employee, year, request.getWorkingDays(), request.getId());
        } else if (action == LeaveAction.MANAGER_REJECT || action == LeaveAction.HR_REJECT || action == LeaveAction.CANCEL) {
            balanceService.recordRelease(employee, year, request.getWorkingDays(), request.getId());
        }
    }
}

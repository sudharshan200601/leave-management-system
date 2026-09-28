package com.leave.management.approval.scheduler;

import com.leave.management.approval.model.ApprovalDecision;
import com.leave.management.approval.model.ApprovalLevel;
import com.leave.management.approval.model.ApprovalStep;
import com.leave.management.approval.repository.ApprovalStepRepository;
import com.leave.management.approval.service.LeaveWorkflowService;
import com.leave.management.common.config.LeaveProperties;
import com.leave.management.common.model.AuditLog;
import com.leave.management.common.repository.AuditLogRepository;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.repository.LeaveRequestRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveWorkflowService workflowService;
    private final ApprovalStepRepository approvalStepRepository;
    private final AuditLogRepository auditLogRepository;
    private final LeaveProperties leaveProperties;
    private final Clock clock;

    public EscalationScheduler(LeaveRequestRepository leaveRequestRepository,
                               LeaveWorkflowService workflowService,
                               ApprovalStepRepository approvalStepRepository,
                               AuditLogRepository auditLogRepository,
                               LeaveProperties leaveProperties,
                               Clock clock) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.workflowService = workflowService;
        this.approvalStepRepository = approvalStepRepository;
        this.auditLogRepository = auditLogRepository;
        this.leaveProperties = leaveProperties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "#{${leave.escalation.check-interval-minutes:15} * 60000}")
    @SchedulerLock(name = "EscalationScheduler_checkAndEscalate", lockAtMostFor = "10m", lockAtLeastFor = "10s")
    @Transactional
    public void checkAndEscalate() {
        LocalDateTime now = LocalDateTime.now(clock);
        log.info("Running automatic escalation check at {}", now);

        int managerTimeoutHours = leaveProperties.getEscalation().getManagerTimeoutHours();
        LocalDateTime managerCutoff = now.minusHours(managerTimeoutHours);

        List<LeaveRequest> expiredManagerRequests = leaveRequestRepository.findExpiredPendingManagerRequests(managerCutoff);
        for (LeaveRequest request : expiredManagerRequests) {
            try {
                workflowService.transition(
                        request.getId(),
                        LeaveAction.ESCALATE,
                        null,
                        "Auto-escalated to HR due to manager timeout (" + managerTimeoutHours + "h)"
                );
                log.info("Auto-escalated request #{} from PENDING_MANAGER to PENDING_HR", request.getId());
            } catch (Exception e) {
                log.error("Failed to auto-escalate request #{}", request.getId(), e);
            }
        }

        int hrTimeoutHours = leaveProperties.getEscalation().getHrTimeoutHours();
        LocalDateTime hrCutoff = now.minusHours(hrTimeoutHours);

        List<LeaveRequest> expiredHrRequests = leaveRequestRepository.findExpiredPendingHrRequests(hrCutoff);
        for (LeaveRequest request : expiredHrRequests) {
            try {
                request.setEscalated(true);
                request.setLastActionAt(now);
                leaveRequestRepository.save(request);

                ApprovalStep step = ApprovalStep.builder()
                        .leaveRequest(request)
                        .level(ApprovalLevel.HR)
                        .approver(null)
                        .decision(ApprovalDecision.AUTO_ESCALATED)
                        .comment("Auto-escalated: HR approval timeout reached (" + hrTimeoutHours + "h)")
                        .actedAt(now)
                        .build();
                approvalStepRepository.save(step);

                AuditLog auditLog = AuditLog.builder()
                        .entityType("LeaveRequest")
                        .entityId(request.getId())
                        .action("AUTO_ESCALATE_HR")
                        .fromStatus(request.getStatus())
                        .toStatus(request.getStatus())
                        .performedBy("SYSTEM")
                        .timestamp(now)
                        .build();
                auditLogRepository.save(auditLog);

                log.info("Auto-escalated request #{} for HR timeout", request.getId());
            } catch (Exception e) {
                log.error("Failed to process HR escalation for request #{}", request.getId(), e);
            }
        }
    }
}

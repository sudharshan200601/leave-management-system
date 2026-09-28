package com.leave.management.approval.scheduler;

import com.leave.management.approval.repository.ApprovalStepRepository;
import com.leave.management.approval.service.LeaveWorkflowService;
import com.leave.management.common.config.LeaveProperties;
import com.leave.management.common.repository.AuditLogRepository;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EscalationSchedulerTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveWorkflowService workflowService;

    @Mock
    private ApprovalStepRepository approvalStepRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    private LeaveProperties leaveProperties;
    private Clock fixedClock;
    private EscalationScheduler scheduler;

    @BeforeEach
    void setUp() {
        leaveProperties = new LeaveProperties();
        leaveProperties.getEscalation().setManagerTimeoutHours(24);
        leaveProperties.getEscalation().setHrTimeoutHours(72);

        // Fixed clock at 2026-10-10T12:00:00Z
        fixedClock = Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneId.of("UTC"));

        scheduler = new EscalationScheduler(
                leaveRequestRepository,
                workflowService,
                approvalStepRepository,
                auditLogRepository,
                leaveProperties,
                fixedClock
        );
    }

    @Test
    @DisplayName("Request older than manager timeout escalates exactly once")
    void testManagerTimeoutEscalatesOnce() {
        LocalDateTime cutoff = LocalDateTime.now(fixedClock).minusHours(24);

        LeaveRequest expiredRequest = LeaveRequest.builder()
                .id(101L)
                .status(LeaveStatus.PENDING_MANAGER)
                .lastActionAt(cutoff.minusHours(2)) // 26 hours old
                .escalated(false)
                .build();

        when(leaveRequestRepository.findExpiredPendingManagerRequests(any()))
                .thenReturn(List.of(expiredRequest));
        when(leaveRequestRepository.findExpiredPendingHrRequests(any()))
                .thenReturn(List.of());

        scheduler.checkAndEscalate();

        verify(workflowService, times(1)).transition(
                eq(101L),
                eq(LeaveAction.ESCALATE),
                isNull(),
                contains("Auto-escalated to HR due to manager timeout")
        );
    }
}

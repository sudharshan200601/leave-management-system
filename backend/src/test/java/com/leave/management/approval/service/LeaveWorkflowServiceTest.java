package com.leave.management.approval.service;

import com.leave.management.approval.repository.ApprovalStepRepository;
import com.leave.management.balance.service.BalanceService;
import com.leave.management.common.exception.IllegalStateTransitionException;
import com.leave.management.common.exception.UnauthorizedAccessException;
import com.leave.management.common.repository.AuditLogRepository;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.model.Role;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveWorkflowServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private ApprovalStepRepository approvalStepRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private LeaveWorkflowService workflowService;

    private Employee employee;
    private Employee manager;
    private Employee hr;
    private LeaveRequest request;

    @BeforeEach
    void setUp() {
        manager = Employee.builder().id(2L).email("manager@company.com").role(Role.MANAGER).teamId(1L).build();
        employee = Employee.builder().id(3L).email("emp@company.com").role(Role.EMPLOYEE).manager(manager).teamId(1L).build();
        hr = Employee.builder().id(1L).email("hr@company.com").role(Role.HR).teamId(1L).build();

        request = LeaveRequest.builder()
                .id(100L)
                .employee(employee)
                .leaveType(com.leave.management.leave.model.LeaveType.ANNUAL)
                .status(LeaveStatus.PENDING_MANAGER)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(7))
                .workingDays(new BigDecimal("3.0"))
                .submittedAt(LocalDateTime.now())
                .lastActionAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Valid transition: PENDING_MANAGER -> MANAGER_APPROVE -> PENDING_HR")
    void testManagerApprove() {
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequest updated = workflowService.transition(100L, LeaveAction.MANAGER_APPROVE, manager, "Looks good");

        assertEquals(LeaveStatus.PENDING_HR, updated.getStatus());
        verify(approvalStepRepository).save(any());
        verify(auditLogRepository).save(any());
    }

    @Test
    @DisplayName("Valid transition: PENDING_HR -> HR_APPROVE -> APPROVED")
    void testHrApprove() {
        request.setStatus(LeaveStatus.PENDING_HR);
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequest updated = workflowService.transition(100L, LeaveAction.HR_APPROVE, hr, "Final approval");

        assertEquals(LeaveStatus.APPROVED, updated.getStatus());
        verify(balanceService).recordApproval(eq(employee), eq(2026), eq(new BigDecimal("3.0")), eq(100L));
    }

    @Test
    @DisplayName("Valid transition: APPROVED -> CANCEL (Future start date) -> CANCELLED")
    void testCancelApprovedFutureLeave() {
        request.setStatus(LeaveStatus.APPROVED);
        request.setStartDate(LocalDate.now().plusDays(10));
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequest updated = workflowService.transition(100L, LeaveAction.CANCEL, employee, "Plans changed");

        assertEquals(LeaveStatus.CANCELLED, updated.getStatus());
        verify(balanceService).recordRelease(eq(employee), eq(2026), eq(new BigDecimal("3.0")), eq(100L));
    }

    @Test
    @DisplayName("Invalid transition: APPROVED -> CANCEL (Today or past start date) throws IllegalStateTransitionException")
    void testCancelApprovedPastLeaveThrows() {
        request.setStatus(LeaveStatus.APPROVED);
        request.setStartDate(LocalDate.now()); // Today
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateTransitionException.class, () ->
                workflowService.transition(100L, LeaveAction.CANCEL, employee, "Too late"));
    }

    @Test
    @DisplayName("Invalid transition: REJECTED status allows no further actions")
    void testTerminalStateNoTransitions() {
        request.setStatus(LeaveStatus.REJECTED);
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateTransitionException.class, () ->
                workflowService.transition(100L, LeaveAction.MANAGER_APPROVE, manager, "Retry"));
    }

    @Test
    @DisplayName("Ownership check: Manager cannot approve their own request")
    void testManagerCannotApproveOwnRequest() {
        request.setEmployee(manager); // Request belongs to manager
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(request));

        assertThrows(UnauthorizedAccessException.class, () ->
                workflowService.transition(100L, LeaveAction.MANAGER_APPROVE, manager, "Self approve"));
    }
}

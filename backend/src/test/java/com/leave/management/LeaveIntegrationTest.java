package com.leave.management;

import com.leave.management.approval.service.LeaveWorkflowService;
import com.leave.management.balance.dto.BalanceResponseDto;
import com.leave.management.balance.service.BalanceService;
import com.leave.management.common.exception.UnauthorizedAccessException;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.repository.EmployeeRepository;
import com.leave.management.leave.dto.CreateLeaveRequestDto;
import com.leave.management.leave.dto.LeaveRequestResponseDto;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.model.LeaveType;
import com.leave.management.leave.service.LeaveApplicationService;
import com.leave.management.leave.service.LeaveDtoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LeaveIntegrationTest {

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    @Autowired
    private LeaveWorkflowService workflowService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveDtoMapper dtoMapper;

    @Test
    @DisplayName("Full Workflow: Submit -> Manager Approve -> HR Approve -> Balance Check")
    void testFullApprovalFlowAndBalance() {
        Employee emp1 = employeeRepository.findByEmail("emp1@company.com").orElseThrow();
        Employee mgr1 = employeeRepository.findByEmail("manager1@company.com").orElseThrow();
        Employee hr = employeeRepository.findByEmail("hr@company.com").orElseThrow();

        // 1. Employee applies for 3 working days leave
        LocalDate start = LocalDate.of(2026, 11, 2); // Monday
        LocalDate end = LocalDate.of(2026, 11, 4);   // Wednesday (3 working days)

        CreateLeaveRequestDto dto = new CreateLeaveRequestDto(start, end, LeaveType.ANNUAL, "Vacation trip");
        LeaveRequest request = leaveApplicationService.applyForLeave(emp1, dto);

        assertNotNull(request.getId());
        assertEquals(LeaveStatus.PENDING_MANAGER, request.getStatus());
        assertEquals(new BigDecimal("3.0"), request.getWorkingDays());

        // Check balance during PENDING_MANAGER (3 days reserved)
        BalanceResponseDto initialBalance = balanceService.getBalance(emp1, 2026);
        assertEquals(new BigDecimal("24.0"), initialBalance.getEntitlement());
        assertEquals(new BigDecimal("3.0"), initialBalance.getReserved());
        assertEquals(new BigDecimal("0.0"), initialBalance.getUsed());
        assertEquals(new BigDecimal("21.0"), initialBalance.getAvailable());

        // 2. Manager approves
        LeaveRequest afterManager = workflowService.transition(request.getId(), LeaveAction.MANAGER_APPROVE, mgr1, "Manager approved");
        assertEquals(LeaveStatus.PENDING_HR, afterManager.getStatus());

        // 3. HR approves
        LeaveRequest afterHr = workflowService.transition(request.getId(), LeaveAction.HR_APPROVE, hr, "HR approved");
        assertEquals(LeaveStatus.APPROVED, afterHr.getStatus());

        // 4. Final Balance check (3 days converted to DEBIT)
        BalanceResponseDto finalBalance = balanceService.getBalance(emp1, 2026);
        assertEquals(new BigDecimal("24.0"), finalBalance.getEntitlement());
        assertEquals(new BigDecimal("0.0"), finalBalance.getReserved());
        assertEquals(new BigDecimal("3.0"), finalBalance.getUsed());
        assertEquals(new BigDecimal("21.0"), finalBalance.getAvailable());

        LeaveRequestResponseDto responseDto = dtoMapper.toResponseDto(afterHr);
        assertEquals(2, responseDto.getApprovalSteps().size());
    }

    @Test
    @DisplayName("Security & Access Control: Manager cannot approve another team's request")
    void testManagerCannotApproveOtherTeamRequest() {
        Employee emp1 = employeeRepository.findByEmail("emp1@company.com").orElseThrow(); // Team 1
        Employee mgr2 = employeeRepository.findByEmail("manager2@company.com").orElseThrow(); // Team 2 Manager

        LocalDate start = LocalDate.of(2026, 11, 9);
        LocalDate end = LocalDate.of(2026, 11, 10);
        CreateLeaveRequestDto dto = new CreateLeaveRequestDto(start, end, LeaveType.ANNUAL, "Personal leave");
        LeaveRequest request = leaveApplicationService.applyForLeave(emp1, dto);

        assertThrows(UnauthorizedAccessException.class, () ->
                workflowService.transition(request.getId(), LeaveAction.MANAGER_APPROVE, mgr2, "Cross-team approval attempt"));
    }
}

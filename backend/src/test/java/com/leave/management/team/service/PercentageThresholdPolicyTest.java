package com.leave.management.team.service;

import com.leave.management.common.config.LeaveProperties;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.repository.EmployeeRepository;
import com.leave.management.leave.dto.ConflictResult;
import com.leave.management.leave.model.ConflictSeverity;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PercentageThresholdPolicyTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    private LeaveProperties leaveProperties;

    @InjectMocks
    private PercentageThresholdPolicy policy;

    private Employee candidateEmp;
    private LeaveRequest candidateRequest;

    @BeforeEach
    void setUp() {
        leaveProperties = new LeaveProperties();
        leaveProperties.getConflict().setMaxTeamAbsencePercent(30.0);
        policy = new PercentageThresholdPolicy(employeeRepository, leaveRequestRepository, leaveProperties);

        candidateEmp = Employee.builder().id(10L).name("Applicant").teamId(1L).build();
        candidateRequest = LeaveRequest.builder()
                .id(100L)
                .employee(candidateEmp)
                .startDate(LocalDate.of(2026, 10, 5))
                .endDate(LocalDate.of(2026, 10, 9))
                .build();

        when(employeeRepository.countByTeamId(1L)).thenReturn(6L);
    }

    @Test
    @DisplayName("No overlap: Only candidate absent (1 of 6 = 16.7% <= 30%) -> severity NONE, flag false")
    void testNoOverlap() {
        when(leaveRequestRepository.findOverlappingTeamRequests(eq(1L), any(), any(), any()))
                .thenReturn(List.of());

        ConflictResult result = policy.evaluate(candidateRequest);

        assertFalse(result.isConflictFlag());
        assertEquals(ConflictSeverity.NONE, result.getSeverity());
        assertEquals(1, result.getPeakAbsentCount());
        assertEquals(6, result.getTeamSize());
    }

    @Test
    @DisplayName("Partial overlap exceeding threshold: 2 of 6 absent (33.3% > 30%) -> severity HIGH, flag true")
    void testPartialOverlapExceedingThreshold() {
        Employee teammate = Employee.builder().id(11L).name("Teammate 1").teamId(1L).build();
        LeaveRequest teammateReq = LeaveRequest.builder()
                .id(200L)
                .employee(teammate)
                .startDate(LocalDate.of(2026, 10, 5))
                .endDate(LocalDate.of(2026, 10, 6))
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveRequestRepository.findOverlappingTeamRequests(eq(1L), any(), any(), any()))
                .thenReturn(List.of(teammateReq));

        ConflictResult result = policy.evaluate(candidateRequest);

        assertTrue(result.isConflictFlag());
        assertEquals(ConflictSeverity.HIGH, result.getSeverity());
        assertEquals(2, result.getPeakAbsentCount());
    }

    @Test
    @DisplayName("Full overlap exceeding threshold: 3 of 6 absent (50.0% > 30%) -> severity HIGH, flag true")
    void testFullOverlapExceedingThreshold() {
        Employee t1 = Employee.builder().id(11L).name("T1").teamId(1L).build();
        Employee t2 = Employee.builder().id(12L).name("T2").teamId(1L).build();

        LeaveRequest req1 = LeaveRequest.builder().id(201L).employee(t1).startDate(LocalDate.of(2026, 10, 5)).endDate(LocalDate.of(2026, 10, 9)).build();
        LeaveRequest req2 = LeaveRequest.builder().id(202L).employee(t2).startDate(LocalDate.of(2026, 10, 5)).endDate(LocalDate.of(2026, 10, 9)).build();

        when(leaveRequestRepository.findOverlappingTeamRequests(eq(1L), any(), any(), any()))
                .thenReturn(List.of(req1, req2));

        ConflictResult result = policy.evaluate(candidateRequest);

        assertTrue(result.isConflictFlag());
        assertEquals(ConflictSeverity.HIGH, result.getSeverity());
        assertEquals(3, result.getPeakAbsentCount());
        assertTrue(result.getDetails().contains("3 of 6 team members absent"));
    }
}

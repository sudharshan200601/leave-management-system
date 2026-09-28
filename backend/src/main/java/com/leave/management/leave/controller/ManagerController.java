package com.leave.management.leave.controller;

import com.leave.management.approval.dto.ApprovalActionDto;
import com.leave.management.approval.service.LeaveWorkflowService;
import com.leave.management.employee.model.Employee;
import com.leave.management.leave.dto.LeaveRequestResponseDto;
import com.leave.management.leave.dto.TeamCalendarDto;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.repository.LeaveRequestRepository;
import com.leave.management.leave.service.LeaveApplicationService;
import com.leave.management.leave.service.LeaveDtoMapper;
import com.leave.management.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'HR')")
public class ManagerController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveWorkflowService workflowService;
    private final LeaveApplicationService leaveApplicationService;
    private final LeaveDtoMapper dtoMapper;

    public ManagerController(LeaveRequestRepository leaveRequestRepository,
                             LeaveWorkflowService workflowService,
                             LeaveApplicationService leaveApplicationService,
                             LeaveDtoMapper dtoMapper) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.workflowService = workflowService;
        this.leaveApplicationService = leaveApplicationService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestResponseDto>> getPendingTeamRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        Employee manager = principal.getEmployee();
        List<LeaveRequest> requests = leaveRequestRepository.findPendingByManagerId(manager.getId());
        List<LeaveRequestResponseDto> response = requests.stream()
                .map(dtoMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponseDto> approveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalActionDto body) {
        String comment = (body != null && body.getComment() != null) ? body.getComment() : "Approved by Manager";
        LeaveRequest request = workflowService.transition(id, LeaveAction.MANAGER_APPROVE, principal.getEmployee(), comment);
        return ResponseEntity.ok(dtoMapper.toResponseDto(request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponseDto> rejectRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalActionDto body) {
        String comment = (body != null && body.getComment() != null) ? body.getComment() : "Rejected by Manager";
        LeaveRequest request = workflowService.transition(id, LeaveAction.MANAGER_REJECT, principal.getEmployee(), comment);
        
        leaveApplicationService.reevaluateTeamConflicts(
                request.getEmployee().getTeamId(), request.getStartDate(), request.getEndDate());

        return ResponseEntity.ok(dtoMapper.toResponseDto(request));
    }

    @GetMapping("/team-calendar")
    public ResponseEntity<List<TeamCalendarDto>> getTeamCalendar(
            @AuthenticationPrincipal UserPrincipal principal) {
        Employee manager = principal.getEmployee();
        List<LeaveRequest> requests = leaveRequestRepository.findByTeamId(manager.getTeamId());
        List<TeamCalendarDto> calendar = requests.stream()
                .map(dtoMapper::toTeamCalendarDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(calendar);
    }
}

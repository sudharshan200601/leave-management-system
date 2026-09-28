package com.leave.management.leave.controller;

import com.leave.management.approval.service.LeaveWorkflowService;
import com.leave.management.balance.dto.BalanceResponseDto;
import com.leave.management.balance.service.BalanceService;
import com.leave.management.common.exception.ResourceNotFoundException;
import com.leave.management.common.exception.UnauthorizedAccessException;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.model.Role;
import com.leave.management.leave.dto.CreateLeaveRequestDto;
import com.leave.management.leave.dto.LeaveRequestResponseDto;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.repository.LeaveRequestRepository;
import com.leave.management.leave.service.LeaveApplicationService;
import com.leave.management.leave.service.LeaveDtoMapper;
import com.leave.management.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveApplicationService leaveApplicationService;
    private final LeaveWorkflowService workflowService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final BalanceService balanceService;
    private final LeaveDtoMapper dtoMapper;

    public LeaveController(LeaveApplicationService leaveApplicationService,
                           LeaveWorkflowService workflowService,
                           LeaveRequestRepository leaveRequestRepository,
                           BalanceService balanceService,
                           LeaveDtoMapper dtoMapper) {
        this.leaveApplicationService = leaveApplicationService;
        this.workflowService = workflowService;
        this.leaveRequestRepository = leaveRequestRepository;
        this.balanceService = balanceService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<LeaveRequestResponseDto> applyForLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLeaveRequestDto dto) {
        LeaveRequest request = leaveApplicationService.applyForLeave(principal.getEmployee(), dto);
        return new ResponseEntity<>(dtoMapper.toResponseDto(request), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<List<LeaveRequestResponseDto>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeIdOrderBySubmittedAtDesc(principal.getEmployee().getId());
        List<LeaveRequestResponseDto> response = requests.stream()
                .map(dtoMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<LeaveRequestResponseDto> getRequestById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        Employee current = principal.getEmployee();
        boolean isOwner = request.getEmployee().getId().equals(current.getId());
        boolean isManager = current.getRole() == Role.MANAGER && request.getEmployee().getTeamId().equals(current.getTeamId());
        boolean isHr = current.getRole() == Role.HR;

        if (!isOwner && !isManager && !isHr) {
            throw new UnauthorizedAccessException("You do not have permission to view this leave request");
        }

        return ResponseEntity.ok(dtoMapper.toResponseDto(request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<LeaveRequestResponseDto> cancelRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelled by user") String comment) {
        LeaveRequest request = workflowService.transition(id, LeaveAction.CANCEL, principal.getEmployee(), comment);
        
        leaveApplicationService.reevaluateTeamConflicts(
                request.getEmployee().getTeamId(), request.getStartDate(), request.getEndDate());

        return ResponseEntity.ok(dtoMapper.toResponseDto(request));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<BalanceResponseDto> getBalance(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        BalanceResponseDto balance = balanceService.getBalance(principal.getEmployee(), targetYear);
        return ResponseEntity.ok(balance);
    }
}

package com.leave.management.leave.controller;

import com.leave.management.approval.dto.ApprovalActionDto;
import com.leave.management.approval.service.LeaveWorkflowService;
import com.leave.management.balance.dto.BalanceResponseDto;
import com.leave.management.balance.service.BalanceService;
import com.leave.management.common.exception.ResourceNotFoundException;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.repository.EmployeeRepository;
import com.leave.management.leave.dto.HolidayDto;
import com.leave.management.leave.dto.LeaveRequestResponseDto;
import com.leave.management.leave.model.Holiday;
import com.leave.management.leave.model.LeaveAction;
import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import com.leave.management.leave.repository.HolidayRepository;
import com.leave.management.leave.repository.LeaveRequestRepository;
import com.leave.management.leave.service.LeaveApplicationService;
import com.leave.management.employee.dto.EmployeeAdminDto;
import com.leave.management.employee.service.AdminEmployeeService;
import com.leave.management.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.leave.management.leave.service.LeaveDtoMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hr")
@PreAuthorize("hasRole('HR')")
public class HrController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final HolidayRepository holidayRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveWorkflowService workflowService;
    private final LeaveApplicationService leaveApplicationService;
    private final BalanceService balanceService;
    private final LeaveDtoMapper dtoMapper;
    private final AdminEmployeeService adminEmployeeService;

    public HrController(LeaveRequestRepository leaveRequestRepository,
                        HolidayRepository holidayRepository,
                        EmployeeRepository employeeRepository,
                        LeaveWorkflowService workflowService,
                        LeaveApplicationService leaveApplicationService,
                        BalanceService balanceService,
                        LeaveDtoMapper dtoMapper,
                        AdminEmployeeService adminEmployeeService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.holidayRepository = holidayRepository;
        this.employeeRepository = employeeRepository;
        this.workflowService = workflowService;
        this.leaveApplicationService = leaveApplicationService;
        this.balanceService = balanceService;
        this.dtoMapper = dtoMapper;
        this.adminEmployeeService = adminEmployeeService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestResponseDto>> getPendingHrRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findByStatusOrderBySubmittedAtAsc(LeaveStatus.PENDING_HR);
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
        String comment = (body != null && body.getComment() != null) ? body.getComment() : "Approved by HR";
        LeaveRequest request = workflowService.transition(id, LeaveAction.HR_APPROVE, principal.getEmployee(), comment);

        leaveApplicationService.reevaluateTeamConflicts(
                request.getEmployee().getTeamId(), request.getStartDate(), request.getEndDate());

        return ResponseEntity.ok(dtoMapper.toResponseDto(request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponseDto> rejectRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalActionDto body) {
        String comment = (body != null && body.getComment() != null) ? body.getComment() : "Rejected by HR";
        LeaveRequest request = workflowService.transition(id, LeaveAction.HR_REJECT, principal.getEmployee(), comment);

        leaveApplicationService.reevaluateTeamConflicts(
                request.getEmployee().getTeamId(), request.getStartDate(), request.getEndDate());

        return ResponseEntity.ok(dtoMapper.toResponseDto(request));
    }

    @GetMapping("/all-requests")
    public ResponseEntity<List<LeaveRequestResponseDto>> getAllRequests(
            @RequestParam(required = false) LeaveStatus status) {
        List<LeaveRequest> requests;
        if (status != null) {
            requests = leaveRequestRepository.findByStatusOrderBySubmittedAtAsc(status);
        } else {
            requests = leaveRequestRepository.findAll();
        }
        List<LeaveRequestResponseDto> response = requests.stream()
                .map(dtoMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<HolidayDto>> getHolidays() {
        List<Holiday> holidays = holidayRepository.findAll();
        List<HolidayDto> response = holidays.stream()
                .map(dtoMapper::toHolidayDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/holidays")
    public ResponseEntity<HolidayDto> createHoliday(@Valid @RequestBody HolidayDto dto) {
        Holiday holiday = Holiday.builder()
                .date(dto.getDate())
                .name(dto.getName())
                .build();
        Holiday saved = holidayRepository.save(holiday);
        return new ResponseEntity<>(dtoMapper.toHolidayDto(saved), HttpStatus.CREATED);
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Holiday not found with id: " + id);
        }
        holidayRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balances")
    public ResponseEntity<List<BalanceResponseDto>> getAllBalances(
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        List<Employee> employees = employeeRepository.findAll();
        List<BalanceResponseDto> balances = employees.stream()
                .map(emp -> balanceService.getBalance(emp, targetYear))
                .collect(Collectors.toList());
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeAdminDto>> getAllEmployees() {
        return ResponseEntity.ok(adminEmployeeService.getAllEmployees());
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeAdminDto> createEmployee(@RequestBody EmployeeAdminDto dto) {
        return new ResponseEntity<>(adminEmployeeService.createEmployee(dto), HttpStatus.CREATED);
    }

    @PostMapping("/employees/upload")
    public ResponseEntity<List<EmployeeAdminDto>> uploadEmployees(@RequestParam("file") MultipartFile file) {
        try {
            return new ResponseEntity<>(adminEmployeeService.uploadEmployees(file), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

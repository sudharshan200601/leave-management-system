package com.leave.management.leave.service;

import com.leave.management.approval.dto.ApprovalStepDto;
import com.leave.management.approval.model.ApprovalStep;
import com.leave.management.approval.repository.ApprovalStepRepository;
import com.leave.management.leave.dto.HolidayDto;
import com.leave.management.leave.dto.LeaveRequestResponseDto;
import com.leave.management.leave.dto.TeamCalendarDto;
import com.leave.management.leave.model.Holiday;
import com.leave.management.leave.model.LeaveRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeaveDtoMapper {

    private final ApprovalStepRepository approvalStepRepository;

    public LeaveDtoMapper(ApprovalStepRepository approvalStepRepository) {
        this.approvalStepRepository = approvalStepRepository;
    }

    public LeaveRequestResponseDto toResponseDto(LeaveRequest request) {
        List<ApprovalStep> steps = approvalStepRepository.findByLeaveRequestIdOrderByActedAtAsc(request.getId());

        List<ApprovalStepDto> stepDtos = steps.stream()
                .map(this::toApprovalStepDto)
                .collect(Collectors.toList());

        return LeaveRequestResponseDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getName())
                .employeeEmail(request.getEmployee().getEmail())
                .teamId(request.getEmployee().getTeamId())
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .workingDays(request.getWorkingDays())
                .reason(request.getReason())
                .status(request.getStatus())
                .conflictFlag(request.isConflictFlag())
                .conflictSeverity(request.getConflictSeverity())
                .conflictDetails(request.getConflictDetails())
                .submittedAt(request.getSubmittedAt())
                .lastActionAt(request.getLastActionAt())
                .escalated(request.isEscalated())
                .compensatoryWorkingDate(request.getCompensatoryWorkingDate())
                .documentName(request.getDocumentName())
                .documentData(request.getDocumentData())
                .odType(request.getOdType())
                .location(request.getLocation())
                .remarks(request.getRemarks())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .approvalSteps(stepDtos)
                .build();
    }

    public ApprovalStepDto toApprovalStepDto(ApprovalStep step) {
        return ApprovalStepDto.builder()
                .id(step.getId())
                .level(step.getLevel())
                .approverId(step.getApprover() != null ? step.getApprover().getId() : null)
                .approverName(step.getApprover() != null ? step.getApprover().getName() : "SYSTEM (Auto-Escalated)")
                .decision(step.getDecision())
                .comment(step.getComment())
                .actedAt(step.getActedAt())
                .build();
    }

    public HolidayDto toHolidayDto(Holiday holiday) {
        return HolidayDto.builder()
                .id(holiday.getId())
                .date(holiday.getDate())
                .name(holiday.getName())
                .build();
    }

    public TeamCalendarDto toTeamCalendarDto(LeaveRequest request) {
        return TeamCalendarDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getName())
                .teamId(request.getEmployee().getTeamId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .leaveType(request.getLeaveType())
                .build();
    }
}

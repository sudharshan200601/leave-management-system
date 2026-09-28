package com.leave.management.leave.repository;

import com.leave.management.leave.model.LeaveRequest;
import com.leave.management.leave.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderBySubmittedAtDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderBySubmittedAtAsc(LeaveStatus status);

    List<LeaveRequest> findByStatusIn(List<LeaveStatus> statuses);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId AND lr.status = 'PENDING_MANAGER' ORDER BY lr.submittedAt ASC")
    List<LeaveRequest> findPendingByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.teamId = :teamId ORDER BY lr.submittedAt DESC")
    List<LeaveRequest> findByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.teamId = :teamId AND lr.status IN :statuses AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingTeamRequests(
            @Param("teamId") Long teamId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status IN :statuses AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingEmployeeRequests(
            @Param("employeeId") Long employeeId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING_MANAGER' AND lr.lastActionAt <= :cutoffTime")
    List<LeaveRequest> findExpiredPendingManagerRequests(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING_HR' AND lr.lastActionAt <= :cutoffTime AND lr.escalated = false")
    List<LeaveRequest> findExpiredPendingHrRequests(@Param("cutoffTime") LocalDateTime cutoffTime);
}

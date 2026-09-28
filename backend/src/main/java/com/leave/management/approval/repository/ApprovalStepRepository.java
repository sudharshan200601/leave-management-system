package com.leave.management.approval.repository;

import com.leave.management.approval.model.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByLeaveRequestIdOrderByActedAtAsc(Long leaveRequestId);
}

package com.leave.management.swap.repository;

import com.leave.management.swap.model.LeaveSwapRequest;
import com.leave.management.swap.model.SwapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveSwapRequestRepository extends JpaRepository<LeaveSwapRequest, Long> {
    List<LeaveSwapRequest> findByRequesterId(Long requesterId);
    List<LeaveSwapRequest> findByTargetEmployeeId(Long targetEmployeeId);
    List<LeaveSwapRequest> findByTargetEmployeeIdAndStatus(Long targetEmployeeId, SwapStatus status);
    List<LeaveSwapRequest> findByStatus(SwapStatus status);
}

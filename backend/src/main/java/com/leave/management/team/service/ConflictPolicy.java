package com.leave.management.team.service;

import com.leave.management.leave.dto.ConflictResult;
import com.leave.management.leave.model.LeaveRequest;

public interface ConflictPolicy {
    ConflictResult evaluate(LeaveRequest candidate);
}

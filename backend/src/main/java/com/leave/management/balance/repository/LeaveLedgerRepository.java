package com.leave.management.balance.repository;

import com.leave.management.balance.model.LeaveLedger;
import com.leave.management.balance.model.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LeaveLedgerRepository extends JpaRepository<LeaveLedger, Long> {

    List<LeaveLedger> findByEmployeeIdAndYear(Long employeeId, Integer year);

    List<LeaveLedger> findByLeaveRequestId(Long leaveRequestId);

    @Query("SELECT COALESCE(SUM(l.days), 0) FROM LeaveLedger l WHERE l.employee.id = :employeeId AND l.year = :year AND l.entryType = :entryType")
    BigDecimal sumDaysByEmployeeAndYearAndEntryType(
            @Param("employeeId") Long employeeId,
            @Param("year") Integer year,
            @Param("entryType") LedgerEntryType entryType
    );

    boolean existsByEmployeeIdAndYearAndEntryType(Long employeeId, Integer year, LedgerEntryType entryType);
}

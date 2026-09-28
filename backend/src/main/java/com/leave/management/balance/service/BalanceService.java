package com.leave.management.balance.service;

import com.leave.management.balance.dto.BalanceResponseDto;
import com.leave.management.balance.model.LeaveLedger;
import com.leave.management.balance.model.LedgerEntryType;
import com.leave.management.balance.repository.LeaveLedgerRepository;
import com.leave.management.common.config.LeaveProperties;
import com.leave.management.employee.model.Employee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BalanceService {

    private final LeaveLedgerRepository ledgerRepository;
    private final LeaveProperties leaveProperties;

    public BalanceService(LeaveLedgerRepository ledgerRepository, LeaveProperties leaveProperties) {
        this.ledgerRepository = ledgerRepository;
        this.leaveProperties = leaveProperties;
    }

    public BigDecimal calculateEntitlement(Employee employee, int year) {
        int joinYear = employee.getJoinDate().getYear();

        if (joinYear < year) {
            return leaveProperties.getAnnualQuotaDays().setScale(1, RoundingMode.HALF_UP);
        }

        if (joinYear > year) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        int joinMonth = employee.getJoinDate().getMonthValue();
        int joinDay = employee.getJoinDate().getDayOfMonth();

        int remainingMonths = (joinDay <= 15) ? (12 - joinMonth + 1) : (12 - joinMonth);

        if (remainingMonths <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        BigDecimal quota = leaveProperties.getAnnualQuotaDays();
        BigDecimal unrounded = quota.multiply(BigDecimal.valueOf(remainingMonths))
                .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);

        return unrounded.multiply(new BigDecimal("2"))
                .setScale(0, RoundingMode.HALF_UP)
                .divide(new BigDecimal("2"), 1, RoundingMode.HALF_UP);
    }

    @Transactional
    public BalanceResponseDto getBalance(Employee employee, int year) {
        ensureAnnualCredit(employee, year);

        List<LeaveLedger> entries = ledgerRepository.findByEmployeeIdAndYear(employee.getId(), year);

        BigDecimal entitlement = entries.stream()
                .filter(e -> e.getEntryType() == LedgerEntryType.CREDIT)
                .map(LeaveLedger::getDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal reservedTotal = entries.stream()
                .filter(e -> e.getEntryType() == LedgerEntryType.RESERVE)
                .map(LeaveLedger::getDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal releaseTotal = entries.stream()
                .filter(e -> e.getEntryType() == LedgerEntryType.RELEASE)
                .map(LeaveLedger::getDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal reserved = reservedTotal.subtract(releaseTotal);
        if (reserved.compareTo(BigDecimal.ZERO) < 0) {
            reserved = BigDecimal.ZERO;
        }

        BigDecimal used = entries.stream()
                .filter(e -> e.getEntryType() == LedgerEntryType.DEBIT)
                .map(LeaveLedger::getDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = entitlement.subtract(reserved).subtract(used);
        if (available.compareTo(BigDecimal.ZERO) < 0) {
            available = BigDecimal.ZERO;
        }

        return BalanceResponseDto.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .year(year)
                .entitlement(entitlement.setScale(1, RoundingMode.HALF_UP))
                .reserved(reserved.setScale(1, RoundingMode.HALF_UP))
                .used(used.setScale(1, RoundingMode.HALF_UP))
                .available(available.setScale(1, RoundingMode.HALF_UP))
                .build();
    }

    @Transactional
    public void ensureAnnualCredit(Employee employee, int year) {
        boolean exists = ledgerRepository.existsByEmployeeIdAndYearAndEntryType(
                employee.getId(), year, LedgerEntryType.CREDIT);

        if (!exists) {
            BigDecimal entitlement = calculateEntitlement(employee, year);
            LeaveLedger credit = LeaveLedger.builder()
                    .employee(employee)
                    .year(year)
                    .entryType(LedgerEntryType.CREDIT)
                    .days(entitlement)
                    .createdAt(LocalDateTime.now())
                    .build();
            ledgerRepository.save(credit);
        }
    }

    @Transactional
    public void recordReserve(Employee employee, int year, BigDecimal days, Long leaveRequestId) {
        ensureAnnualCredit(employee, year);
        LeaveLedger reserve = LeaveLedger.builder()
                .employee(employee)
                .year(year)
                .entryType(LedgerEntryType.RESERVE)
                .days(days)
                .leaveRequestId(leaveRequestId)
                .createdAt(LocalDateTime.now())
                .build();
        ledgerRepository.save(reserve);
    }

    @Transactional
    public void recordApproval(Employee employee, int year, BigDecimal days, Long leaveRequestId) {
        LeaveLedger debit = LeaveLedger.builder()
                .employee(employee)
                .year(year)
                .entryType(LedgerEntryType.DEBIT)
                .days(days)
                .leaveRequestId(leaveRequestId)
                .createdAt(LocalDateTime.now())
                .build();
        ledgerRepository.save(debit);

        LeaveLedger release = LeaveLedger.builder()
                .employee(employee)
                .year(year)
                .entryType(LedgerEntryType.RELEASE)
                .days(days)
                .leaveRequestId(leaveRequestId)
                .createdAt(LocalDateTime.now())
                .build();
        ledgerRepository.save(release);
    }

    @Transactional
    public void recordRelease(Employee employee, int year, BigDecimal days, Long leaveRequestId) {
        LeaveLedger release = LeaveLedger.builder()
                .employee(employee)
                .year(year)
                .entryType(LedgerEntryType.RELEASE)
                .days(days)
                .leaveRequestId(leaveRequestId)
                .createdAt(LocalDateTime.now())
                .build();
        ledgerRepository.save(release);
    }
}

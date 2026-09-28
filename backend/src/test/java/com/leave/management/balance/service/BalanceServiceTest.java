package com.leave.management.balance.service;

import com.leave.management.balance.repository.LeaveLedgerRepository;
import com.leave.management.common.config.LeaveProperties;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private LeaveLedgerRepository ledgerRepository;

    private LeaveProperties leaveProperties;

    @InjectMocks
    private BalanceService balanceService;

    @BeforeEach
    void setUp() {
        leaveProperties = new LeaveProperties();
        leaveProperties.setAnnualQuotaDays(new BigDecimal("24"));
        balanceService = new BalanceService(ledgerRepository, leaveProperties);
    }

    @Test
    @DisplayName("Proration: January joiner (on or before 15th) receives full 24 days")
    void testJanuaryJoiner() {
        Employee emp = Employee.builder()
                .joinDate(LocalDate.of(2026, 1, 15))
                .role(Role.EMPLOYEE)
                .build();

        BigDecimal entitlement = balanceService.calculateEntitlement(emp, 2026);
        assertEquals(new BigDecimal("24.0"), entitlement);
    }

    @Test
    @DisplayName("Proration: July 10 joiner (on or before 15th) receives 6 months = 12 days")
    void testJuly10Joiner() {
        Employee emp = Employee.builder()
                .joinDate(LocalDate.of(2026, 7, 10))
                .role(Role.EMPLOYEE)
                .build();

        BigDecimal entitlement = balanceService.calculateEntitlement(emp, 2026);
        assertEquals(new BigDecimal("12.0"), entitlement);
    }

    @Test
    @DisplayName("Proration: July 20 joiner (after 15th) receives 5 months = 10 days")
    void testJuly20Joiner() {
        Employee emp = Employee.builder()
                .joinDate(LocalDate.of(2026, 7, 20))
                .role(Role.EMPLOYEE)
                .build();

        BigDecimal entitlement = balanceService.calculateEntitlement(emp, 2026);
        assertEquals(new BigDecimal("10.0"), entitlement);
    }

    @Test
    @DisplayName("Proration: December 20 joiner (after 15th) receives 0 months = 0.0 days")
    void testDecember20Joiner() {
        Employee emp = Employee.builder()
                .joinDate(LocalDate.of(2026, 12, 20))
                .role(Role.EMPLOYEE)
                .build();

        BigDecimal entitlement = balanceService.calculateEntitlement(emp, 2026);
        assertEquals(new BigDecimal("0.0"), entitlement);
    }

    @Test
    @DisplayName("Proration: Prior year joiner receives full annual quota")
    void testPriorYearJoiner() {
        Employee emp = Employee.builder()
                .joinDate(LocalDate.of(2024, 5, 10))
                .role(Role.EMPLOYEE)
                .build();

        BigDecimal entitlement = balanceService.calculateEntitlement(emp, 2026);
        assertEquals(new BigDecimal("24.0"), entitlement);
    }
}

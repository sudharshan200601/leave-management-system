package com.leave.management.leave.service;

import com.leave.management.leave.model.Holiday;
import com.leave.management.leave.repository.HolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkingDaysCalculatorTest {

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private WorkingDaysCalculator calculator;

    @BeforeEach
    void setUp() {
        when(holidayRepository.findByDateBetween(any(), any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Calculate working days for a normal work week (Mon-Fri)")
    void testNormalWorkWeek() {
        // Monday 2026-10-05 to Friday 2026-10-09
        LocalDate start = LocalDate.of(2026, 10, 5);
        LocalDate end = LocalDate.of(2026, 10, 9);

        BigDecimal days = calculator.calculateWorkingDays(start, end);
        assertEquals(new BigDecimal("5.0"), days);
    }

    @Test
    @DisplayName("Calculate working days spanning a weekend")
    void testSpanningWeekend() {
        // Thursday 2026-10-08 to Tuesday 2026-10-13 (Thu, Fri, Sat, Sun, Mon, Tue)
        // Working: Thu, Fri, Mon, Tue = 4 days
        LocalDate start = LocalDate.of(2026, 10, 8);
        LocalDate end = LocalDate.of(2026, 10, 13);

        BigDecimal days = calculator.calculateWorkingDays(start, end);
        assertEquals(new BigDecimal("4.0"), days);
    }

    @Test
    @DisplayName("Calculate working days excluding holidays")
    void testExcludingHolidays() {
        // Monday 2026-10-05 to Friday 2026-10-09, with Wednesday as holiday
        LocalDate start = LocalDate.of(2026, 10, 5);
        LocalDate end = LocalDate.of(2026, 10, 9);
        LocalDate holidayDate = LocalDate.of(2026, 10, 7);

        when(holidayRepository.findByDateBetween(start, end)).thenReturn(
                List.of(Holiday.builder().date(holidayDate).name("Midweek Holiday").build())
        );

        BigDecimal days = calculator.calculateWorkingDays(start, end);
        assertEquals(new BigDecimal("4.0"), days);
    }
}

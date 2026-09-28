package com.leave.management.leave.service;

import com.leave.management.leave.repository.HolidayRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WorkingDaysCalculator {

    private final HolidayRepository holidayRepository;

    public WorkingDaysCalculator(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    public BigDecimal calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return BigDecimal.ZERO;
        }

        Set<LocalDate> holidays = holidayRepository.findByDateBetween(startDate, endDate).stream()
                .map(h -> h.getDate())
                .collect(Collectors.toSet());

        int workingDaysCount = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !holidays.contains(date)) {
                workingDaysCount++;
            }
        }

        return BigDecimal.valueOf(workingDaysCount).setScale(1, RoundingMode.HALF_UP);
    }
}

package com.leave.management.leave.repository;

import com.leave.management.leave.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    Optional<Holiday> findByDate(LocalDate date);

    boolean existsByDate(LocalDate date);

    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);
}

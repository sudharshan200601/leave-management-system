package com.leave.management.employee.repository;

import com.leave.management.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByTeamId(Long teamId);

    List<Employee> findByManagerId(Long managerId);

    long countByTeamId(Long teamId);
}

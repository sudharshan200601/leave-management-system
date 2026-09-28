package com.leave.management.common.config;

import com.leave.management.balance.service.BalanceService;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.model.Role;
import com.leave.management.employee.repository.EmployeeRepository;
import com.leave.management.leave.model.Holiday;
import com.leave.management.leave.repository.HolidayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;
    private final BalanceService balanceService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository,
                           HolidayRepository holidayRepository,
                           BalanceService balanceService,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.holidayRepository = holidayRepository;
        this.balanceService = balanceService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            log.info("Data already seeded. Skipping initialization.");
            return;
        }

        log.info("Seeding initial data...");
        String encodedPassword = passwordEncoder.encode("password123");

        Employee hr = Employee.builder()
                .name("Helen Rogers (HR)")
                .email("hr@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.HR)
                .teamId(1L)
                .joinDate(LocalDate.of(2024, 1, 1))
                .build();
        hr = employeeRepository.save(hr);

        Employee mgr1 = Employee.builder()
                .name("Sarah Jenkins (Manager T1)")
                .email("manager1@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.MANAGER)
                .manager(hr)
                .teamId(1L)
                .joinDate(LocalDate.of(2024, 1, 1))
                .build();
        mgr1 = employeeRepository.save(mgr1);

        Employee mgr2 = Employee.builder()
                .name("Michael Scott (Manager T2)")
                .email("manager2@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.MANAGER)
                .manager(hr)
                .teamId(2L)
                .joinDate(LocalDate.of(2024, 1, 1))
                .build();
        mgr2 = employeeRepository.save(mgr2);

        Employee emp1 = Employee.builder()
                .name("Alice Smith (Team 1)")
                .email("emp1@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.EMPLOYEE)
                .manager(mgr1)
                .teamId(1L)
                .joinDate(LocalDate.of(2025, 1, 15))
                .build();

        Employee emp2 = Employee.builder()
                .name("Bob Johnson (Mid-Year <=15th)")
                .email("emp2@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.EMPLOYEE)
                .manager(mgr1)
                .teamId(1L)
                .joinDate(LocalDate.of(2026, 7, 10))
                .build();

        Employee emp3 = Employee.builder()
                .name("Charlie Davis (Mid-Year >15th)")
                .email("emp3@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.EMPLOYEE)
                .manager(mgr1)
                .teamId(1L)
                .joinDate(LocalDate.of(2026, 7, 20))
                .build();

        Employee emp4 = Employee.builder()
                .name("David Wilson (Team 2)")
                .email("emp4@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.EMPLOYEE)
                .manager(mgr2)
                .teamId(2L)
                .joinDate(LocalDate.of(2026, 1, 1))
                .build();

        Employee emp5 = Employee.builder()
                .name("Eve Adams (Team 2 Late Joiner)")
                .email("emp5@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.EMPLOYEE)
                .manager(mgr2)
                .teamId(2L)
                .joinDate(LocalDate.of(2026, 12, 1))
                .build();

        Employee emp6 = Employee.builder()
                .name("Frank Thomas (Team 2)")
                .email("emp6@company.com")
                .passwordHash(encodedPassword)
                .plainPassword("password123")
                .role(Role.EMPLOYEE)
                .manager(mgr2)
                .teamId(2L)
                .joinDate(LocalDate.of(2026, 1, 10))
                .build();

        List<Employee> employees = List.of(hr, mgr1, mgr2, emp1, emp2, emp3, emp4, emp5, emp6);
        employeeRepository.saveAll(employees);

        for (Employee emp : employees) {
            balanceService.ensureAnnualCredit(emp, 2026);
        }

        holidayRepository.saveAll(List.of(
                Holiday.builder().date(LocalDate.of(2026, 1, 1)).name("New Year's Day").build(),
                Holiday.builder().date(LocalDate.of(2026, 7, 4)).name("Independence Day").build(),
                Holiday.builder().date(LocalDate.of(2026, 11, 26)).name("Thanksgiving Day").build(),
                Holiday.builder().date(LocalDate.of(2026, 12, 25)).name("Christmas Day").build()
        ));

        log.info("Sample data initialized successfully.");
    }
}

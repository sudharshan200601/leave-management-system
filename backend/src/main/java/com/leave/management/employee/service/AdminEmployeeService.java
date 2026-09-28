package com.leave.management.employee.service;

import com.leave.management.balance.service.BalanceService;
import com.leave.management.employee.dto.EmployeeAdminDto;
import com.leave.management.employee.model.Employee;
import com.leave.management.employee.model.Role;
import com.leave.management.employee.repository.EmployeeRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BalanceService balanceService;

    public AdminEmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, BalanceService balanceService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.balanceService = balanceService;
    }

    public List<EmployeeAdminDto> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public EmployeeAdminDto createEmployee(EmployeeAdminDto dto) {
        Employee manager = null;
        if (dto.getManagerId() != null) {
            manager = employeeRepository.findById(dto.getManagerId()).orElse(null);
        }

        Employee employee = Employee.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPlainPassword()))
                .plainPassword(dto.getPlainPassword())
                .role(dto.getRole())
                .manager(manager)
                .teamId(dto.getTeamId())
                .joinDate(dto.getJoinDate() != null ? dto.getJoinDate() : LocalDate.now())
                .build();

        Employee saved = employeeRepository.save(employee);
        balanceService.ensureAnnualCredit(saved, LocalDate.now().getYear());
        return toDto(saved);
    }

    @Transactional
    public List<EmployeeAdminDto> uploadEmployees(MultipartFile file) throws Exception {
        List<EmployeeAdminDto> created = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) continue;

                String name = row.getCell(0).getStringCellValue();
                String email = row.getCell(1).getStringCellValue();
                String password = row.getCell(2).getStringCellValue();
                String roleStr = row.getCell(3).getStringCellValue();
                Long teamId = (long) row.getCell(4).getNumericCellValue();
                Double managerIdDouble = row.getCell(5) != null ? row.getCell(5).getNumericCellValue() : null;
                Long managerId = managerIdDouble != null && managerIdDouble > 0 ? managerIdDouble.longValue() : null;
                
                String dateStr = row.getCell(6) != null ? row.getCell(6).getStringCellValue() : null;
                LocalDate joinDate = dateStr != null ? LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now();

                EmployeeAdminDto dto = new EmployeeAdminDto();
                dto.setName(name);
                dto.setEmail(email);
                dto.setPlainPassword(password);
                dto.setRole(Role.valueOf(roleStr));
                dto.setTeamId(teamId);
                dto.setManagerId(managerId);
                dto.setJoinDate(joinDate);

                created.add(createEmployee(dto));
            }
        }
        return created;
    }

    private EmployeeAdminDto toDto(Employee emp) {
        EmployeeAdminDto dto = new EmployeeAdminDto();
        dto.setId(emp.getId());
        dto.setName(emp.getName());
        dto.setEmail(emp.getEmail());
        dto.setPlainPassword(emp.getPlainPassword());
        dto.setRole(emp.getRole());
        dto.setManagerId(emp.getManager() != null ? emp.getManager().getId() : null);
        dto.setTeamId(emp.getTeamId());
        dto.setJoinDate(emp.getJoinDate());
        return dto;
    }
}

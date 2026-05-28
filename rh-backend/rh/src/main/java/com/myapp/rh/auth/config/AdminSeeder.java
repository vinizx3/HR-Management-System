package com.myapp.rh.auth.config;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.entity.Role;
import com.myapp.rh.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (employeeRepository.findByEmail("admin@rh.com").isEmpty()) {

            Employee admin = Employee.builder()
                    .name("Admin")
                    .email("admin@rh.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.HR_MANAGER)
                    .department("RH")
                    .salary(BigDecimal.valueOf(10000))
                    .hireDate(LocalDate.now())
                    .active(true)
                    .build();

            employeeRepository.save(admin);

            System.out.println("ADMIN CREATED");
        }
    }
}

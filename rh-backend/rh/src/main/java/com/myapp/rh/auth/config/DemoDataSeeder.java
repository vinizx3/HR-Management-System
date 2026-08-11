package com.myapp.rh.auth.config;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.entity.Role;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.timeclock.entity.TimeRecord;
import com.myapp.rh.timeclock.entity.TimeRecordStatus;
import com.myapp.rh.timeclock.repository.TimeRecordRepository;
import com.myapp.rh.vacation.entity.VacationRequest;
import com.myapp.rh.vacation.entity.VacationStatus;
import com.myapp.rh.vacation.repository.VacationRequestRepository;
import com.myapp.rh.overtime.entity.OvertimeBalance;
import com.myapp.rh.overtime.repository.OvertimeBalanceRepository;
import com.myapp.rh.notification.entity.Notification;
import com.myapp.rh.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
public class DemoDataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final VacationRequestRepository vacationRequestRepository;
    private final OvertimeBalanceRepository overtimeBalanceRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_ADMIN_EMAIL = "demo.admin@rh.com";
    private static final String DEMO_EMPLOYEE_EMAIL = "demo.employee@rh.com";

    @Override
    public void run(String... args) {

        if (employeeRepository.findByEmail(DEMO_ADMIN_EMAIL).isPresent()) {
            return;
        }

        Employee demoAdmin = Employee.builder()
                .name("Demo Admin")
                .email(DEMO_ADMIN_EMAIL)
                .password(passwordEncoder.encode("demo123"))
                .role(Role.DEMO_ADMIN)
                .department("RH")
                .salary(BigDecimal.valueOf(9000))
                .hireDate(LocalDate.now().minusMonths(6))
                .active(true)
                .build();
        employeeRepository.save(demoAdmin);

        Employee demoEmployee = Employee.builder()
                .name("Demo Employee")
                .email(DEMO_EMPLOYEE_EMAIL)
                .password(passwordEncoder.encode("demo123"))
                .role(Role.DEMO_EMPLOYEE)
                .department("Tecnologia")
                .salary(BigDecimal.valueOf(5000))
                .hireDate(LocalDate.now().minusMonths(3))
                .active(true)
                .build();
        employeeRepository.save(demoEmployee);

        for (int i = 10; i >= 1; i--) {
            LocalDate day = LocalDate.now().minusDays(i);

            if (day.getDayOfWeek().getValue() >= 6) continue;

            LocalDateTime clockIn = day.atTime(8, 0);
            boolean overtimeDay = i % 3 == 0;
            LocalDateTime clockOut = overtimeDay ? day.atTime(18, 30) : day.atTime(17, 0);

            int workedMinutes = (int) java.time.Duration.between(clockIn, clockOut).toMinutes();
            int overtimeMinutes = overtimeDay ? workedMinutes - 480 : 0;

            TimeRecord record = TimeRecord.builder()
                    .employee(demoEmployee)
                    .date(day)
                    .clockIn(clockIn)
                    .clockOut(clockOut)
                    .workedMinutes(workedMinutes)
                    .overtimeMinutes(overtimeMinutes)
                    .status(TimeRecordStatus.CLOSED)
                    .build();

            timeRecordRepository.save(record);
        }

        OvertimeBalance overtimeBalance = OvertimeBalance.builder()
                .employee(demoEmployee)
                .totalMinutes(210) 
                .build();
        overtimeBalanceRepository.save(overtimeBalance);

        VacationRequest approved = VacationRequest.builder()
                .employee(demoEmployee)
                .startDate(LocalDate.now().plusDays(20))
                .endDate(LocalDate.now().plusDays(30))
                .vacationStatus(VacationStatus.APPROVED)
                .requestedAt(LocalDateTime.now().minusDays(15))
                .reviewedBy(demoAdmin)
                .build();

        VacationRequest pending = VacationRequest.builder()
                .employee(demoEmployee)
                .startDate(LocalDate.now().plusDays(60))
                .endDate(LocalDate.now().plusDays(75))
                .vacationStatus(VacationStatus.PENDING)
                .requestedAt(LocalDateTime.now().minusDays(2))
                .build();

        VacationRequest rejected = VacationRequest.builder()
                .employee(demoEmployee)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .vacationStatus(VacationStatus.REJECTED)
                .requestedAt(LocalDateTime.now().minusDays(10))
                .reviewedBy(demoAdmin)
                .build();

        vacationRequestRepository.saveAll(List.of(approved, pending, rejected));

        Notification approvedNotification = Notification.builder()
                .employeeEmail(DEMO_EMPLOYEE_EMAIL)
                .message("Sua solicitação de férias foi aprovada.")
                .read(false)
                .createdAt(LocalDateTime.now().minusDays(14))
                .build();

        Notification rejectedNotification = Notification.builder()
                .employeeEmail(DEMO_EMPLOYEE_EMAIL)
                .message("Sua solicitação de férias foi rejeitada.")
                .read(true)
                .createdAt(LocalDateTime.now().minusDays(9))
                .build();

        notificationRepository.saveAll(List.of(approvedNotification, rejectedNotification));

        System.out.println("DEMO DATA SEEDED: demo.admin@rh.com / demo.employee@rh.com (senha: demo123)");
    }
}
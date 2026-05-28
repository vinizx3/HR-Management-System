package com.myapp.rh.timeclock.service;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.exception.ResourceNotFoundException;
import com.myapp.rh.overtime.service.OvertimeBalanceService;
import com.myapp.rh.timeclock.dto.TimeRecordResponseDTO;
import com.myapp.rh.timeclock.entity.TimeRecord;
import com.myapp.rh.timeclock.entity.TimeRecordStatus;
import com.myapp.rh.timeclock.entity.WorkTimeCalculator;
import com.myapp.rh.timeclock.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TimeClockService {

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final OvertimeBalanceService overtimeBalanceService;
    private final Clock clock;

    public TimeRecordResponseDTO clockIn(String email) {

        LocalDate today = LocalDate.now(clock);
        Employee employee = getEmployeeByEmail(email);

        validateNoOpenRecord(employee.getId(), today);

        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setEmployee(employee);
        timeRecord.setDate(today);
        timeRecord.setClockIn(LocalDateTime.now(clock));
        timeRecord.setStatus(TimeRecordStatus.OPEN);

        return toDTO(timeRecordRepository.save(timeRecord));
    }

    public TimeRecordResponseDTO clockOut(String email) {

        LocalDate today = LocalDate.now(clock);
        Employee employee = getEmployeeByEmail(email);

        TimeRecord timeRecord = timeRecordRepository
                .findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new ResourceNotFoundException("Point not found"));

        if (timeRecord.getStatus() != TimeRecordStatus.OPEN) {
            throw new BusinessException("Point already closed");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        timeRecord.setClockOut(now);

        long minutesWorked = ChronoUnit.MINUTES.between(
                timeRecord.getClockIn(), now);

        timeRecord.setWorkedMinutes((int) minutesWorked);

        int overtime = WorkTimeCalculator.calculateOvertime(minutesWorked);
        timeRecord.setOvertimeMinutes(overtime);

        if (overtime > 0) {
            overtimeBalanceService.addOvertime(employee, overtime);
        }

        timeRecord.setStatus(TimeRecordStatus.CLOSED);

        return toDTO(timeRecordRepository.save(timeRecord));
    }

    @Transactional(readOnly = true)
    public List<TimeRecordResponseDTO> getMyRecords(String email) {

        Employee employee = getEmployeeByEmail(email);

        return timeRecordRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeRecordResponseDTO> getEmployeeRecords(UUID employeeId) {

        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));

        return timeRecordRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private TimeRecordResponseDTO toDTO(TimeRecord timeRecord) {

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern("HH:mm");

        String date = timeRecord.getDate().format(dateFormatter);
        String clockIn = timeRecord.getClockIn().format(timeFormatter);

        String clockOut = timeRecord.getClockOut() != null
                ? timeRecord.getClockOut().format(timeFormatter)
                : null;

        String workedTime = timeRecord.getWorkedMinutes() != null
                ? WorkTimeCalculator.formatMinutes(timeRecord.getWorkedMinutes())
                : null;

        String overtimeTime = timeRecord.getOvertimeMinutes() != null
                ? WorkTimeCalculator.formatMinutes(timeRecord.getOvertimeMinutes())
                : null;

        return new TimeRecordResponseDTO(
                timeRecord.getId(),
                date,
                clockIn,
                clockOut,
                timeRecord.getWorkedMinutes(),
                workedTime,
                timeRecord.getOvertimeMinutes(),
                overtimeTime,
                timeRecord.getStatus()
        );
    }

    private Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found"));
    }

    private void validateNoOpenRecord(UUID employeeId, LocalDate date) {
        if (timeRecordRepository.findByEmployeeIdAndDate(employeeId, date).isPresent()) {
            throw new BusinessException("There is already an open point today");
        }
    }
}

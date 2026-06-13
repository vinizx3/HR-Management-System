package com.myapp.rh;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.exception.ResourceNotFoundException;
import com.myapp.rh.overtime.service.OvertimeBalanceService;
import com.myapp.rh.timeclock.dto.TimeRecordResponseDTO;
import com.myapp.rh.timeclock.entity.TimeRecord;
import com.myapp.rh.timeclock.entity.TimeRecordStatus;
import com.myapp.rh.timeclock.repository.TimeRecordRepository;
import com.myapp.rh.timeclock.service.TimeClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimeClockServiceTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OvertimeBalanceService overtimeBalanceService;

    private Clock clock;
    private TimeClockService timeClockService;

    private UUID employeeId;
    private Employee employee;

    @BeforeEach
    public void setup() {

        clock = Clock.fixed(
                LocalDate.of(2026, 5, 17)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        timeClockService = new TimeClockService(
                timeRecordRepository,
                employeeRepository,
                overtimeBalanceService,
                clock
        );

        employeeId = UUID.randomUUID();

        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", employeeId);
        employee.setName("John Doe");
        employee.setEmail("john@test.com");
    }

    @Test
    public void shouldClockInSuccessfully() {

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findByEmployeeIdAndDateAndStatus(
                eq(employeeId),
                eq(LocalDate.now(clock)),
                eq(TimeRecordStatus.OPEN) ))
                .thenReturn(Optional.empty());

        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(inv -> {
                    TimeRecord tr = inv.getArgument(0);
                    ReflectionTestUtils.setField(tr, "id", UUID.randomUUID());
                    return tr;
                });

        TimeRecordResponseDTO dto = timeClockService.clockIn("john@test.com");

        assertNotNull(dto);
        assertEquals(TimeRecordStatus.OPEN, dto.status());

        verify(timeRecordRepository).save(any(TimeRecord.class));
    }

    @Test
    public void shouldThrowWhenAlreadyClockedIn() {

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findByEmployeeIdAndDateAndStatus(
                eq(employeeId),
                eq(LocalDate.now(clock)),
                eq(TimeRecordStatus.OPEN)
        )).thenReturn(Optional.of(new TimeRecord()));

        assertThrows(BusinessException.class,
                () -> timeClockService.clockIn("john@test.com"));

        verify(timeRecordRepository, never()).save(any());
    }

    @Test
    public void shouldClockOutSuccessfully() {

        TimeRecord openRecord = new TimeRecord();
        ReflectionTestUtils.setField(openRecord, "id", UUID.randomUUID());
        openRecord.setEmployee(employee);
        openRecord.setDate(LocalDate.now(clock));
        openRecord.setClockIn(LocalDateTime.now(clock).minusHours(8));
        openRecord.setStatus(TimeRecordStatus.OPEN);

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findFirstByEmployeeIdAndStatusOrderByDateDesc(
                eq(employeeId),
                eq(TimeRecordStatus.OPEN)
        )).thenReturn(Optional.of(openRecord));

        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenReturn(openRecord);

        TimeRecordResponseDTO responseDTO =
                timeClockService.clockOut("john@test.com");

        assertNotNull(responseDTO);
        assertEquals(TimeRecordStatus.CLOSED, openRecord.getStatus());
        assertNotNull(openRecord.getClockOut());
    }

    @Test
    public void shouldThrowWhenClockOutWithNoOpenRecord() {

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findFirstByEmployeeIdAndStatusOrderByDateDesc(
                eq(employeeId),
                eq(TimeRecordStatus.OPEN)
        )).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> timeClockService.clockOut("john@test.com"));
    }

    @Test
    public void shouldThrowWhenClockOutAlreadyClosed() {

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findFirstByEmployeeIdAndStatusOrderByDateDesc(
                eq(employeeId),
                eq(TimeRecordStatus.OPEN)
        )).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> timeClockService.clockOut("john@test.com"));
    }

    @Test
    public void shouldCalculateOvertimeCorrectly() {

        TimeRecord openRecord = new TimeRecord();
        ReflectionTestUtils.setField(openRecord, "id", UUID.randomUUID());
        openRecord.setEmployee(employee);
        openRecord.setDate(LocalDate.now(clock));
        openRecord.setClockIn(LocalDateTime.of(2026, 5, 17, 8, 0));
        openRecord.setStatus(TimeRecordStatus.OPEN);

        Clock fixedClockOut = Clock.fixed(
                LocalDateTime.of(2026, 5, 17, 17, 30)
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        TimeClockService serviceWithCustomClock = new TimeClockService(
                timeRecordRepository,
                employeeRepository,
                overtimeBalanceService,
                fixedClockOut
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findFirstByEmployeeIdAndStatusOrderByDateDesc(
                any(UUID.class),
                eq(TimeRecordStatus.OPEN)
        )).thenReturn(Optional.of(openRecord));

        when(timeRecordRepository.save(any()))
                .thenReturn(openRecord);

        serviceWithCustomClock.clockOut("john@test.com");

        assertEquals(90, openRecord.getOvertimeMinutes());
        verify(overtimeBalanceService).addOvertime(eq(employee), eq(90));
    }

    @Test
    public void shouldNotAddOvertimeWhenWithinTolerance() {

        TimeRecord openRecord = new TimeRecord();
        ReflectionTestUtils.setField(openRecord, "id", UUID.randomUUID());
        openRecord.setEmployee(employee);
        openRecord.setDate(LocalDate.now(clock));
        openRecord.setClockIn(LocalDateTime.of(2026, 5, 17, 8, 0));
        openRecord.setStatus(TimeRecordStatus.OPEN);

        Clock fixedClockOut = Clock.fixed(
                LocalDateTime.of(2026, 5, 17, 16, 5)
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        TimeClockService serviceWithCustomClock = new TimeClockService(
                timeRecordRepository,
                employeeRepository,
                overtimeBalanceService,
                fixedClockOut
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findFirstByEmployeeIdAndStatusOrderByDateDesc(
                any(UUID.class),
                eq(TimeRecordStatus.OPEN)
        )).thenReturn(Optional.of(openRecord));

        when(timeRecordRepository.save(any()))
                .thenReturn(openRecord);

        serviceWithCustomClock.clockOut("john@test.com");

        assertEquals(0, openRecord.getOvertimeMinutes());
        verify(overtimeBalanceService, never()).addOvertime(any(), any());
    }
}
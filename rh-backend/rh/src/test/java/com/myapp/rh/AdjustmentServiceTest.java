package com.myapp.rh;

import com.myapp.rh.adjustment.dto.AdjustmentRequestDTO;
import com.myapp.rh.adjustment.dto.AdjustmentResponseDTO;
import com.myapp.rh.adjustment.entity.Adjustment;
import com.myapp.rh.adjustment.entity.AdjustmentStatus;
import com.myapp.rh.adjustment.repository.AdjustmentRepository;
import com.myapp.rh.adjustment.service.AdjustmentService;
import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.timeclock.entity.TimeRecord;
import com.myapp.rh.timeclock.entity.TimeRecordStatus;
import com.myapp.rh.timeclock.repository.TimeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdjustmentServiceTest {

    @Mock
    private AdjustmentRepository adjustmentRepository;

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private Clock clock;
    private AdjustmentService adjustmentService;

    private UUID employeeId;
    private UUID timeRecordId;
    private UUID adjustmentId;
    private Employee employee;
    private Employee manager;
    private TimeRecord timeRecord;

    @BeforeEach
    public void setup() {

        clock = Clock.fixed(
                LocalDate.of(2026,5,17)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        adjustmentService = new AdjustmentService(
                adjustmentRepository, timeRecordRepository,
                employeeRepository, clock
        );

        employeeId = UUID.randomUUID();
        timeRecordId = UUID.randomUUID();
        adjustmentId = UUID.randomUUID();

        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", employeeId);
        employee.setName("John Doe");
        employee.setEmail("john@test.com");

        manager = new Employee();
        ReflectionTestUtils.setField(manager, "id", UUID.randomUUID());
        manager.setName("Manager");
        manager.setEmail("manager@test.com");

        timeRecord = new TimeRecord();
        ReflectionTestUtils.setField(timeRecord, "id", timeRecordId);
        timeRecord.setEmployee(employee);
        timeRecord.setDate(LocalDate.of(2026,5,16));
        timeRecord.setClockIn(LocalDateTime.of(2026,5,16,8,0));
        timeRecord.setStatus(TimeRecordStatus.CLOSED);
    }

    @Test
    public void shouldRequestAdjustmentSuccessfully() {

        AdjustmentRequestDTO dto = new AdjustmentRequestDTO(
                timeRecordId,
                LocalDateTime.of(2026,5,16,8,0),
                LocalDateTime.of(2026,5,16,17,0),
                "I forgot to register"
        );

        Adjustment saved = new Adjustment();
        ReflectionTestUtils.setField(saved, "id", adjustmentId);
        saved.setEmployee(employee);
        saved.setTimeRecord(timeRecord);
        saved.setRequestedClockIn(dto.requestedClockIn());
        saved.setRequestedClockOut(dto.requestedClockOut());
        saved.setReason(dto.reason());
        saved.setStatus(AdjustmentStatus.PENDING);
        saved.setRequestedAt(LocalDateTime.now(clock));

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findById(timeRecordId))
                .thenReturn(Optional.of(timeRecord));

        when(adjustmentRepository.findByEmployeeId(employeeId))
                .thenReturn(List.of());

        when(adjustmentRepository.save(any())).thenReturn(saved);

        AdjustmentResponseDTO responseDTO =
                adjustmentService.requestAdjustment("john@test.com", dto);

        assertNotNull(responseDTO);
        assertEquals(AdjustmentStatus.PENDING, responseDTO.status());
        verify(adjustmentRepository).save(any());
    }

    @Test
    public void shouldThrowWhenTimeRecordDoesNotBelongToEmployee() {

        Employee otherEmployee = new Employee();
        ReflectionTestUtils.setField(otherEmployee, "id", UUID.randomUUID());

        TimeRecord otherRecord = new TimeRecord();
        ReflectionTestUtils.setField(otherRecord, "id", timeRecordId);
        otherRecord.setEmployee(otherEmployee);

        AdjustmentRequestDTO dto = new AdjustmentRequestDTO(
                timeRecordId,
                LocalDateTime.of(2026,5,16,8,0),
                LocalDateTime.of(2026,5,16,17,0),
                "reason"
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findById(timeRecordId))
                .thenReturn(Optional.of(otherRecord));

        assertThrows(BusinessException.class,
                () -> adjustmentService.requestAdjustment("john@test.com", dto));

        verify(adjustmentRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenPendingAdjustmentAlreadyExists() {

        Adjustment pending = new Adjustment();
        pending.setTimeRecord(timeRecord);
        pending.setStatus(AdjustmentStatus.PENDING);

        AdjustmentRequestDTO dto = new AdjustmentRequestDTO(
                timeRecordId,
                LocalDateTime.of(2026, 5, 16, 8, 0),
                LocalDateTime.of(2026, 5, 16, 17, 0),
                "Motivo"
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findById(timeRecordId))
                .thenReturn(Optional.of(timeRecord));

        when(adjustmentRepository.findByEmployeeId(employeeId))
                .thenReturn(List.of(pending));

        assertThrows(BusinessException.class,
                () -> adjustmentService.requestAdjustment("john@test.com", dto));
    }

    @Test
    public void shouldApproveAdjustmentAndUpdateTimeRecord() {

        Adjustment adjustment = buildAdjustment(AdjustmentStatus.PENDING);

        when(adjustmentRepository.findById(adjustmentId))
                .thenReturn(Optional.of(adjustment));

        when(employeeRepository.findByEmail("manager@test.com"))
                .thenReturn(Optional.of(manager));

        when(adjustmentRepository.save(any())).thenReturn(adjustment);

        AdjustmentResponseDTO response =
                adjustmentService.approveAdjustment(adjustmentId, "manager@test.com");

        assertEquals(AdjustmentStatus.APPROVED, response.status());
        assertEquals(TimeRecordStatus.ADJUSTED, timeRecord.getStatus());
        verify(timeRecordRepository).save(timeRecord);
    }

    @Test
    public void shouldRejectAdjustmentSuccessfully() {

        Adjustment adjustment = buildAdjustment(AdjustmentStatus.PENDING);

        when(adjustmentRepository.findById(adjustmentId))
                .thenReturn(Optional.of(adjustment));

        when(employeeRepository.findByEmail("manager@test.com"))
                .thenReturn(Optional.of(manager));

        when(adjustmentRepository.save(any())).thenReturn(adjustment);

        AdjustmentResponseDTO response =
                adjustmentService.rejectAdjustment(adjustmentId, "manager@test.com");

        assertEquals(AdjustmentStatus.REJECTED, response.status());
        verify(timeRecordRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenApprovingAlreadyProcessedAdjustment() {

        Adjustment adjustment = buildAdjustment(AdjustmentStatus.APPROVED);

        when(adjustmentRepository.findById(adjustmentId))
                .thenReturn(Optional.of(adjustment));

        assertThrows(BusinessException.class,
                () -> adjustmentService.approveAdjustment(adjustmentId, "manager@test.com"));

        verify(adjustmentRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenClockOutBeforeClockIn() {

        AdjustmentRequestDTO dto = new AdjustmentRequestDTO(
                timeRecordId,
                LocalDateTime.of(2026, 5, 16, 17, 0),
                LocalDateTime.of(2026, 5, 16, 8, 0), // antes do clockIn
                "Motivo"
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(timeRecordRepository.findById(timeRecordId))
                .thenReturn(Optional.of(timeRecord));

        when(adjustmentRepository.findByEmployeeId(employeeId))
                .thenReturn(List.of());

        assertThrows(BusinessException.class,
                () -> adjustmentService.requestAdjustment("john@test.com", dto));
    }

    private Adjustment buildAdjustment(AdjustmentStatus status) {

        Adjustment adjustment = new Adjustment();
        ReflectionTestUtils.setField(adjustment, "id", adjustmentId);
        adjustment.setEmployee(employee);
        adjustment.setTimeRecord(timeRecord);
        adjustment.setRequestedClockIn(LocalDateTime.of(2026, 5, 16, 8, 0));
        adjustment.setRequestedClockOut(LocalDateTime.of(2026, 5, 16, 17, 0));
        adjustment.setReason("Motivo");
        adjustment.setStatus(status);
        adjustment.setRequestedAt(LocalDateTime.now(clock));

        return adjustment;
    }
}

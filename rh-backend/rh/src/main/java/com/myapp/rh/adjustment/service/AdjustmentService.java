package com.myapp.rh.adjustment.service;

import com.myapp.rh.adjustment.dto.AdjustmentRequestDTO;
import com.myapp.rh.adjustment.dto.AdjustmentResponseDTO;
import com.myapp.rh.adjustment.entity.Adjustment;
import com.myapp.rh.adjustment.entity.AdjustmentStatus;
import com.myapp.rh.adjustment.repository.AdjustmentRepository;
import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.exception.ResourceNotFoundException;
import com.myapp.rh.timeclock.entity.TimeRecord;
import com.myapp.rh.timeclock.entity.TimeRecordStatus;
import com.myapp.rh.timeclock.entity.WorkTimeCalculator;
import com.myapp.rh.timeclock.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdjustmentService {

    private final AdjustmentRepository adjustmentRequestRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    public AdjustmentResponseDTO requestAdjustment(
            String email,
            AdjustmentRequestDTO dto) {

        Employee employee = getEmployeeByEmail(email);

        TimeRecord timeRecord = timeRecordRepository
                .findById(dto.timeRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Time record not found: " + dto.timeRecordId()));

        validateOwnership(timeRecord, employee);
        validateNoPendingAdjustment(employee.getId(), dto.timeRecordId());
        validateAdjustmentTimes(dto);

        Adjustment adjustment = Adjustment.builder()
                .timeRecord(timeRecord)
                .employee(employee)
                .requestedClockIn(dto.requestedClockIn())
                .requestedClockOut(dto.requestedClockOut())
                .reason(dto.reason())
                .status(AdjustmentStatus.PENDING)
                .requestedAt(LocalDateTime.now(clock))
                .build();

        Adjustment saved = adjustmentRequestRepository.save(adjustment);

        log.info("Adjustment requested | employee={} | timeRecord={}",
                email, dto.timeRecordId());

        return toDTO(saved);
    }

    public AdjustmentResponseDTO approveAdjustment(
            UUID adjustmentId,
            String managerEmail) {

        Adjustment adjustment = getAdjustmentOrThrow(adjustmentId);
        validatePendingStatus(adjustment);

        Employee manager = getEmployeeByEmail(managerEmail);

        applyAdjustmentToTimeRecord(adjustment);

        adjustment.setStatus(AdjustmentStatus.APPROVED);
        adjustment.setReviewedBy(manager);

        Adjustment saved = adjustmentRequestRepository.save(adjustment);

        log.info("Adjustment approved | adjustmentId={} | manager={}",
                adjustmentId, managerEmail);

        return toDTO(saved);
    }

    public AdjustmentResponseDTO rejectAdjustment(
            UUID adjustmentId,
            String managerEmail) {

        Adjustment adjustment = getAdjustmentOrThrow(adjustmentId);
        validatePendingStatus(adjustment);

        Employee manager = getEmployeeByEmail(managerEmail);

        adjustment.setStatus(AdjustmentStatus.REJECTED);
        adjustment.setReviewedBy(manager);

        Adjustment saved = adjustmentRequestRepository.save(adjustment);

        log.info("Adjustment rejected | adjustmentId={} | manager={}",
                adjustmentId, managerEmail);

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AdjustmentResponseDTO> getMyAdjustments(String email) {

        Employee employee = getEmployeeByEmail(email);

        return adjustmentRequestRepository
                .findByEmployeeId(employee.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdjustmentResponseDTO> getPendingAdjustments() {

        return adjustmentRequestRepository
                .findByStatus(AdjustmentStatus.PENDING)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private void applyAdjustmentToTimeRecord(Adjustment adjustment) {

        TimeRecord timeRecord = adjustment.getTimeRecord();
        timeRecord.setClockIn(adjustment.getRequestedClockIn());
        timeRecord.setClockOut(adjustment.getRequestedClockOut());

        long minutesWorked = ChronoUnit.MINUTES.between(
                adjustment.getRequestedClockIn(),
                adjustment.getRequestedClockOut());

        timeRecord.setWorkedMinutes((int) minutesWorked);
        timeRecord.setOvertimeMinutes(
                WorkTimeCalculator.calculateOvertime(minutesWorked));
        timeRecord.setStatus(TimeRecordStatus.ADJUSTED);

        timeRecordRepository.save(timeRecord);
    }

    private void validateOwnership(TimeRecord timeRecord, Employee employee) {
        if (!timeRecord.getEmployee().getId().equals(employee.getId())) {
            throw new BusinessException(
                    "Time record does not belong to this employee");
        }
    }

    private void validateNoPendingAdjustment(UUID employeeId, UUID timeRecordId) {

        boolean hasPending = adjustmentRequestRepository
                .findByEmployeeId(employeeId)
                .stream()
                .anyMatch(a ->
                        a.getTimeRecord().getId().equals(timeRecordId) &&
                                a.getStatus() == AdjustmentStatus.PENDING);

        if (hasPending) {
            throw new BusinessException(
                    "There is already a pending adjustment for this record");
        }
    }

    private void validateAdjustmentTimes(AdjustmentRequestDTO dto) {

        if (dto.requestedClockOut().isBefore(dto.requestedClockIn())) {
            throw new BusinessException("Clock out cannot be before clock in");
        }

        long minutes = ChronoUnit.MINUTES.between(
                dto.requestedClockIn(), dto.requestedClockOut());

        if (minutes < 1) {
            throw new BusinessException(
                    "Worked time must be at least 1 minute");
        }
    }

    private void validatePendingStatus(Adjustment adjustment) {
        if (adjustment.getStatus() != AdjustmentStatus.PENDING) {
            throw new BusinessException("Adjustment already processed");
        }
    }

    private Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found"));
    }

    private Adjustment getAdjustmentOrThrow(UUID id) {
        return adjustmentRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Adjustment not found: " + id));
    }

    private AdjustmentResponseDTO toDTO(Adjustment adjustment) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return new AdjustmentResponseDTO(
                adjustment.getId(),
                adjustment.getTimeRecord().getId(),
                adjustment.getEmployee().getName(),
                adjustment.getRequestedClockIn().format(formatter),
                adjustment.getRequestedClockOut().format(formatter),
                adjustment.getReason(),
                adjustment.getStatus(),
                adjustment.getRequestedAt().format(formatter)
        );
    }
}

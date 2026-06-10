package com.myapp.rh.vacation.service;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.event.VacationReviewedEvent;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.exception.ResourceNotFoundException;
import com.myapp.rh.producer.VacationEventProducerPort;
import com.myapp.rh.vacation.dto.VacationRequestDTO;
import com.myapp.rh.vacation.dto.VacationResponseDTO;
import com.myapp.rh.vacation.entity.VacationRequest;
import com.myapp.rh.vacation.entity.VacationStatus;
import com.myapp.rh.vacation.repository.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class VacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final VacationEventProducerPort vacationEventProducer;
    private final Clock clock;

    public VacationResponseDTO requestVacation(
            String email,
            VacationRequestDTO request) {

        Employee employee = getEmployeeByEmail(email);
        validateVacationDates(request);

        VacationRequest vacationRequest = createVacationRequest(employee, request);
        VacationRequest saved = vacationRequestRepository.save(vacationRequest);

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<VacationResponseDTO> getMyVacations(String email) {

        Employee employee = getEmployeeByEmail(email);

        return vacationRequestRepository
                .findByEmployeeId(employee.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VacationResponseDTO> getPendingRequests() {

        return vacationRequestRepository
                .findByVacationStatus(VacationStatus.PENDING)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VacationResponseDTO> getAllVacations() {

        return vacationRequestRepository
                .findAllByOrderByRequestedAtDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public VacationResponseDTO approveRequest(
            UUID vacationId,
            String managerEmail) {

        VacationRequest request = getVacationRequestById(vacationId);
        validatePendingStatus(request);

        Employee manager = getEmployeeByEmail(managerEmail);
        VacationRequest saved = processVacationRequest(
                request, manager, VacationStatus.APPROVED);

        vacationEventProducer.sendVacationReviewedEvent(buildEvent(saved));

        return toDTO(saved);
    }

    public VacationResponseDTO rejectRequest(
            UUID vacationId,
            String managerEmail) {

        VacationRequest request = getVacationRequestById(vacationId);
        validatePendingStatus(request);

        Employee manager = getEmployeeByEmail(managerEmail);
        VacationRequest saved = processVacationRequest(
                request, manager, VacationStatus.REJECTED);

        vacationEventProducer.sendVacationReviewedEvent(buildEvent(saved));

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<VacationResponseDTO> getExpiringVacations(int daysAhead) {

        LocalDate today = LocalDate.now(clock);
        LocalDate limitDate = today.plusDays(daysAhead);

        return vacationRequestRepository
                .findExpiringVacations(today, limitDate)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private VacationRequest createVacationRequest(
            Employee employee,
            VacationRequestDTO request) {

        VacationRequest vacationRequest = new VacationRequest();
        vacationRequest.setEmployee(employee);
        vacationRequest.setStartDate(request.startDate());
        vacationRequest.setEndDate(request.endDate());
        vacationRequest.setVacationStatus(VacationStatus.PENDING);
        vacationRequest.setRequestedAt(LocalDateTime.now(clock));

        return vacationRequest;
    }

    private VacationRequest processVacationRequest(
            VacationRequest request,
            Employee manager,
            VacationStatus vacationStatus) {

        request.setVacationStatus(vacationStatus);
        request.setReviewedBy(manager);

        return vacationRequestRepository.save(request);
    }

    private void validateVacationDates(VacationRequestDTO request) {

        if (request.startDate().isBefore(LocalDate.now(clock))) {
            throw new BusinessException("Vacation cannot start in the past");
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        long daysUntilStart = ChronoUnit.DAYS.between(
                LocalDate.now(clock), request.startDate());

        if (daysUntilStart < 30) {
            throw new BusinessException(
                    "Vacation must be requested at least 30 days in advance");
        }

        long vacationDays = ChronoUnit.DAYS.between(
                request.startDate(), request.endDate());

        if (vacationDays > 30) {
            throw new BusinessException(
                    "Vacation period cannot exceed 30 days");
        }

        if (vacationDays < 1) {
            throw new BusinessException("Vacation must be at least 1 day");
        }
    }

    private void validatePendingStatus(VacationRequest request) {
        if (request.getVacationStatus() != VacationStatus.PENDING) {
            throw new BusinessException("Vacation request already processed");
        }
    }

    private VacationReviewedEvent buildEvent(VacationRequest request) {
        return new VacationReviewedEvent(
                request.getId(),
                request.getEmployee().getName(),
                request.getEmployee().getEmail(),
                request.getVacationStatus().name()
        );
    }

    private Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found"));
    }

    private VacationRequest getVacationRequestById(UUID vacationId) {
        return vacationRequestRepository.findByIdWithEmployee(vacationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vacation request not found"));
    }

    private VacationResponseDTO toDTO(VacationRequest request) {

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        long vacationDays = ChronoUnit.DAYS.between(
                request.getStartDate(), request.getEndDate());

        return new VacationResponseDTO(
                request.getId(),
                request.getEmployee().getName(),
                request.getStartDate().format(dateFormatter),
                request.getEndDate().format(dateFormatter),
                (int) vacationDays,
                request.getVacationStatus(),
                request.getRequestedAt().format(dateTimeFormatter)
        );
    }
}

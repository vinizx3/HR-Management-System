package com.myapp.rh;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.entity.Role;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.producer.VacationEventProducer;
import com.myapp.rh.vacation.dto.VacationRequestDTO;
import com.myapp.rh.vacation.dto.VacationResponseDTO;
import com.myapp.rh.vacation.entity.VacationRequest;
import com.myapp.rh.vacation.entity.VacationStatus;
import com.myapp.rh.vacation.repository.VacationRequestRepository;
import com.myapp.rh.vacation.service.VacationRequestService;
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
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VacationRequestServiceTest {

    @Mock
    private VacationRequestRepository vacationRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private VacationEventProducer vacationEventProducer;

    private Clock clock;
    private VacationRequestService vacationRequestService;

    private UUID employeeId;
    private UUID vacationId;
    private Employee employee;
    private Employee manager;

    @BeforeEach
    public void setup() {

        clock = Clock.fixed(
                LocalDate.of(2026, 5, 17)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        vacationRequestService = new VacationRequestService(
                vacationRequestRepository,
                employeeRepository,
                vacationEventProducer,
                clock
        );

        employeeId = UUID.randomUUID();
        vacationId = UUID.randomUUID();

        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", employeeId);
        employee.setName("John Doe");
        employee.setEmail("john@test.com");

        manager = new Employee();
        ReflectionTestUtils.setField(manager, "id", UUID.randomUUID());
        manager.setName("Manager");
        manager.setEmail("manager@test.com");
        manager.setRole(Role.HR_MANAGER);
    }

    @Test
    public void shouldRequestVacationSuccessfully() {

        VacationRequestDTO dto = new VacationRequestDTO(
                LocalDate.of(2026,7,1),
                LocalDate.of(2026,7,15)
        );

        VacationRequest saved = new VacationRequest();
        ReflectionTestUtils.setField(saved, "id", vacationId);
        saved.setEmployee(employee);
        saved.setStartDate(dto.startDate());
        saved.setEndDate(dto.endDate());
        saved.setVacationStatus(VacationStatus.PENDING);
        saved.setRequestedAt(LocalDateTime.now(clock));

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(vacationRequestRepository.save(any()))
                .thenReturn(saved);

        VacationResponseDTO responseDTO =
                vacationRequestService.requestVacation("john@test.com", dto);

        assertNotNull(responseDTO);
        assertEquals(VacationStatus.PENDING, responseDTO.vacationStatus());
        verify(vacationRequestRepository).save(any());
    }

    @Test
    public void shouldThrowWhenStartDateInPast() {

        VacationRequestDTO dto = new VacationRequestDTO(
                LocalDate.of(2026,5,1),
                LocalDate.of(2026,5,15)
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        assertThrows(BusinessException.class,
                () -> vacationRequestService.requestVacation("john@test.com", dto));

        verify(vacationRequestRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenLessThan30DaysInAdvance() {

        VacationRequestDTO dto = new VacationRequestDTO(
                LocalDate.of(2026,5,20),
                LocalDate.of(2026,6,5)
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        assertThrows(BusinessException.class,
                () -> vacationRequestService.requestVacation("john@test.com", dto));
    }

    @Test
    public void shouldThrowWhenVacationExceeds30days() {

        VacationRequestDTO dto = new VacationRequestDTO(
                LocalDate.of(2026,7,1),
                LocalDate.of(2026,8,5)
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        assertThrows(BusinessException.class,
                () -> vacationRequestService.requestVacation("john@test.com", dto));
    }

    @Test
    public void shouldThrowWenEndDateBeforeStartDate() {

        VacationRequestDTO dto = new VacationRequestDTO(
                LocalDate.of(2026,7,15),
                LocalDate.of(2026,7,1)
        );

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        assertThrows(BusinessException.class,
                () -> vacationRequestService.requestVacation("john@test.com", dto));
    }

    @Test
    public void shouldedApproveVacationAndSendEvent() {

        VacationRequest request = buildVacation(VacationStatus.PENDING);

        when(vacationRequestRepository.findByIdWithEmployee(vacationId))
                .thenReturn(Optional.of(request));

        when(employeeRepository.findByEmail("manager@test.com"))
                .thenReturn(Optional.of(manager));

        when(vacationRequestRepository.save(any())).thenReturn(request);

        VacationResponseDTO responseDTO =
                vacationRequestService.approveRequest(vacationId, "manager@test.com");

        assertEquals(VacationStatus.APPROVED, responseDTO.vacationStatus());

        verify(vacationEventProducer).sendVacationReviewedEvent(
                argThat(event -> event.status().equals("APPROVED"))
        );
    }

    @Test
    public void shouldRejectVacationAndSendEvent() {

        VacationRequest request = buildVacation(VacationStatus.PENDING);

        when(vacationRequestRepository.findByIdWithEmployee(vacationId))
                .thenReturn(Optional.of(request));

        when(employeeRepository.findByEmail("manager@test.com"))
                .thenReturn(Optional.of(manager));

        when(vacationRequestRepository.save(any())).thenReturn(request);

        VacationResponseDTO responseDTO =
                vacationRequestService.rejectRequest(vacationId, "manager@test.com");

        assertEquals(VacationStatus.REJECTED, responseDTO.vacationStatus());

        verify(vacationEventProducer).sendVacationReviewedEvent(
                argThat(event -> event.status().equals("REJECTED")));
    }

    @Test
    public void shouldThrowWhenApprovingAlreadyProcessedVacation() {

        VacationRequest request = buildVacation(VacationStatus.APPROVED);

        when(vacationRequestRepository.findByIdWithEmployee(vacationId))
                .thenReturn(Optional.of(request));

        assertThrows(BusinessException.class,
                () -> vacationRequestService.approveRequest(vacationId, "manager@test.com"));

        verify(vacationRequestRepository, never()).save(any());
        verify(vacationEventProducer, never()).sendVacationReviewedEvent(any());
    }

    private VacationRequest buildVacation(VacationStatus status) {

        VacationRequest request = new VacationRequest();
        ReflectionTestUtils.setField(request, "id", vacationId);
        request.setEmployee(employee);
        request.setStartDate(LocalDate.of(2026,7,1));
        request.setEndDate(LocalDate.of(2026,7,15));
        request.setVacationStatus(status);
        request.setRequestedAt(LocalDateTime.now(clock));

        return request;
    }
}
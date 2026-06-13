package com.myapp.rh;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.overtime.dto.OvertimeBalanceResponseDTO;
import com.myapp.rh.overtime.dto.OvertimeCompensationRequestDTO;
import com.myapp.rh.overtime.entity.OvertimeBalance;
import com.myapp.rh.overtime.repository.OvertimeBalanceRepository;
import com.myapp.rh.overtime.service.OvertimeBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OvertimeBalanceServiceTest {

    @Mock
    private OvertimeBalanceRepository overtimeBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private OvertimeBalanceService overtimeBalanceService;

    private UUID employeeId;
    private Employee employee;

    @BeforeEach
    public void setup() {

        overtimeBalanceService = new OvertimeBalanceService(
                overtimeBalanceRepository, employeeRepository
        );

        employeeId = UUID.randomUUID();
        employee = new Employee();
        ReflectionTestUtils.setField(employee,"id", employeeId);
        employee.setName("John Doe");
        employee.setEmail("john@test.com");
    }

    @Test
    public void shouldAddOvertimeSuccessfully() {

        OvertimeBalance balance = new OvertimeBalance();
        balance.setEmployee(employee);
        balance.setTotalMinutes(60);

        when(overtimeBalanceRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(balance));

        overtimeBalanceService.addOvertime(employee, 30);

        assertEquals(90, balance.getTotalMinutes());
        verify(overtimeBalanceRepository).save(balance);
    }

    @Test
    public void shouldCreateBalanceWhenNotExists() {

        when(overtimeBalanceRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        when(overtimeBalanceRepository.save(any())).thenAnswer(inv -> {
            OvertimeBalance b = inv.getArgument(0);
            ReflectionTestUtils.setField(b, "id", UUID.randomUUID());
            return b;
        });

        overtimeBalanceService.addOvertime(employee, 60);

        verify(overtimeBalanceRepository, times(2)).save(any());
    }

    @Test
    public void shouldCompensateOvertimeSucessfully() {

        OvertimeBalance balance = new OvertimeBalance();
        balance.setEmployee(employee);
        balance.setTotalMinutes(120);

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(overtimeBalanceRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(balance));

        OvertimeCompensationRequestDTO dto =
                new OvertimeCompensationRequestDTO(60, "Medical consultation");

        OvertimeBalanceResponseDTO responseDTO =
                overtimeBalanceService.compensate("john@test.com", dto);

        assertEquals(60, balance.getTotalMinutes());
        assertEquals("01h 00min", responseDTO.formatedBalance());
    }

    @Test
    void shouldThrowWhenInsufficientBalance() {

        OvertimeBalance balance = new OvertimeBalance();
        balance.setEmployee(employee);
        balance.setTotalMinutes(30);

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(overtimeBalanceRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(balance));

        OvertimeCompensationRequestDTO dto =
                new OvertimeCompensationRequestDTO(60, "Absence");

        assertThrows(BusinessException.class,
                () -> overtimeBalanceService.compensate("john@test.com", dto));

        verify(overtimeBalanceRepository, never()).save(any());
    }


    @Test
    void shouldFormatBalanceCorrectly() {

        OvertimeBalance balance = new OvertimeBalance();
        balance.setEmployee(employee);
        balance.setTotalMinutes(90);

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        when(overtimeBalanceRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(balance));

        OvertimeBalanceResponseDTO response =
                overtimeBalanceService.getMyBalance("john@test.com");

        assertEquals("01h 30min", response.formatedBalance());
        assertEquals(90, response.totalMinutes());
    }
}

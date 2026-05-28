package com.myapp.rh;

import com.myapp.rh.employee.dto.EmployeeRequestDTO;
import com.myapp.rh.employee.dto.EmployeeResponseDTO;
import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.entity.Role;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.employee.service.EmployeeService;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmployeeService employeeService;
    private Clock clock;

    private UUID employeeId;
    private Employee employee;

    @BeforeEach
    public void setup() {

        clock = Clock.fixed(
                LocalDate.of(2026, 5,17)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        employeeService = new EmployeeService(
                employeeRepository,
                passwordEncoder,
                clock);
        employeeId = UUID.randomUUID();
        employee = new Employee();

        ReflectionTestUtils.setField(employee, "id", employeeId);

        employee.setName("John Doe");
        employee.setEmail("john@test.com");
        employee.setPassword("encoded");
        employee.setRole(Role.EMPLOYEE);
        employee.setDepartment("Employee");
        employee.setSalary(new BigDecimal("3000"));
        employee.setActive(true);
    }

    @Test
    public void shouldCreateEmployeeSuccessfully(){

        EmployeeRequestDTO dto = new EmployeeRequestDTO();

        dto.setName("John Doe");
        dto.setEmail("john@test.com");
        dto.setPassword("123456");
        dto.setRole(Role.EMPLOYEE);
        dto.setDepartment("Employee");
        dto.setSalary(new BigDecimal("3000"));

        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDTO responseDTO = employeeService.create(dto);

        assertEquals("John Doe", responseDTO.getName());
        assertEquals("john@test.com", responseDTO.getEmail());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    public void shouldThrowWhenEmailAlreadyExists() {

        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setEmail("john@test.com");

        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(BusinessException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    public void shouldFindAllActiveEmployees() {

        when(employeeRepository.findAllByActiveTrue()).thenReturn(List.of(employee));

        List<EmployeeResponseDTO> responseDTOS = employeeService.findAll();

        assertEquals(1, responseDTOS.size());
        assertEquals("John Doe", responseDTOS.get(0).getName());
    }

    @Test
    public void ShouldFindEmployeeById() {

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        EmployeeResponseDTO responseDTO = employeeService.findById(employeeId);

        assertEquals(employeeId, responseDTO.getId());
    }

    @Test
    public void shouldThrowWhenEmployeeNotFoundById() {

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.findById(employeeId));
    }

    @Test
    public void shouldFindEmployeeByEmail() {

        when(employeeRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(employee));

        EmployeeResponseDTO responseDTO = employeeService.findMe("john@test.com");

        assertEquals("john@test.com", responseDTO.getEmail());
    }

    @Test
    public void shouldUpdateEmployeeSuccessfully() {

        EmployeeRequestDTO dto = new EmployeeRequestDTO();

        dto.setName("John Doe");
        dto.setEmail("john@test.com");
        dto.setRole(Role.HR_MANAGER);
        dto.setDepartment("RH");
        dto.setSalary(new BigDecimal("8000"));

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.update(employeeId, dto);

        assertEquals("John Doe", employee.getName());
        assertEquals(Role.HR_MANAGER, employee.getRole());
        verify(employeeRepository).save(employee);
    }

    @Test
    public void shouldDeactivateEmployeeOnDelete() {

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        employeeService.delete(employeeId);

        assertFalse(employee.isActive());
        verify(employeeRepository).save(employee);
    }

    @Test
    public void shouldThrowWhenDeletingNonExistentEmployee() {

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.delete(employeeId));
        verify(employeeRepository, never()).save(any());
    }
}

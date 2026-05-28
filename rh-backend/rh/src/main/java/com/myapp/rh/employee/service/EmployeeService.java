package com.myapp.rh.employee.service;

import com.myapp.rh.employee.dto.EmployeeRequestDTO;
import com.myapp.rh.employee.dto.EmployeeResponseDTO;
import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public EmployeeResponseDTO create(EmployeeRequestDTO dto) {

        validateEmail(dto.getEmail());

        Employee employee = buildEmployee(dto);

        Employee saved = employeeRepository.save(employee);

        return toResponse(saved);
    }

    public List<EmployeeResponseDTO> findAll() {
        return employeeRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponseDTO findById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + id));
        return toResponse(employee);
    }

    public EmployeeResponseDTO findMe(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found"));
        return toResponse(employee);
    }

    public EmployeeResponseDTO update(UUID id, EmployeeRequestDTO dto) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + id));

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setSalary(dto.getSalary());

        return toResponse(employeeRepository.save(employee));
    }

    public void delete(UUID id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + id));

        employee.setActive(false);
        employeeRepository.save(employee);
    }

    private EmployeeResponseDTO toResponse(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .salary(employee.getSalary())
                .active(employee.isActive())
                .build();
    }

    private void validateEmail(String email) {
        if (employeeRepository.existsByEmail(email)) {
            throw new BusinessException("Email already registered");
        }
    }

    private Employee buildEmployee(EmployeeRequestDTO dto) {
        return Employee.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .department(dto.getDepartment())
                .salary(dto.getSalary())
                .hireDate(LocalDate.now(clock))
                .active(true)
                .build();
    }
}

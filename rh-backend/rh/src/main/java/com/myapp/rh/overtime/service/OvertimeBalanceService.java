package com.myapp.rh.overtime.service;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import com.myapp.rh.exception.BusinessException;
import com.myapp.rh.exception.ResourceNotFoundException;
import com.myapp.rh.overtime.dto.OvertimeBalanceResponseDTO;
import com.myapp.rh.overtime.dto.OvertimeCompensationRequestDTO;
import com.myapp.rh.overtime.entity.OvertimeBalance;
import com.myapp.rh.overtime.repository.OvertimeBalanceRepository;
import com.myapp.rh.timeclock.entity.WorkTimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OvertimeBalanceService {

    private final OvertimeBalanceRepository overtimeBalanceRepository;
    private final EmployeeRepository employeeRepository;

    public void addOvertime(Employee employee, Integer minutes) {

        OvertimeBalance balance = getOrCreateBalance(employee);
        balance.setTotalMinutes(balance.getTotalMinutes() + minutes);
        overtimeBalanceRepository.save(balance);
    }

    @Transactional(readOnly = true)
    public OvertimeBalanceResponseDTO getMyBalance(String email) {

        Employee employee = getEmployeeByEmail(email);
        OvertimeBalance balance = getOrCreateBalance(employee);
        return toDTO(balance);
    }

    @Transactional(readOnly = true)
    public OvertimeBalanceResponseDTO getBalanceByEmployeeId(UUID employeeId) {

        Employee employee = getEmployeeById(employeeId);
        OvertimeBalance balance = getOrCreateBalance(employee);
        return toDTO(balance);
    }

    public OvertimeBalanceResponseDTO compensate(
            String email,
            OvertimeCompensationRequestDTO dto) {

        Employee employee = getEmployeeByEmail(email);
        OvertimeBalance balance = getOrCreateBalance(employee);

        if (balance.getTotalMinutes() < dto.minutes()) {
            throw new BusinessException(
                    "Insufficient overtime balance. Available: " +
                            WorkTimeCalculator.formatMinutes(balance.getTotalMinutes()));
        }

        balance.setTotalMinutes(balance.getTotalMinutes() - dto.minutes());
        overtimeBalanceRepository.save(balance);

        log.info("Overtime compensated | employee={} | minutes={} | reason={}",
                email, dto.minutes(), dto.reason());

        return toDTO(balance);
    }

    private OvertimeBalance getOrCreateBalance(Employee employee) {

        return overtimeBalanceRepository
                .findByEmployeeId(employee.getId())
                .orElseGet(() -> {
                    OvertimeBalance newBalance = new OvertimeBalance();
                    newBalance.setEmployee(employee);
                    newBalance.setTotalMinutes(0);
                    return overtimeBalanceRepository.save(newBalance);
                });
    }

    private OvertimeBalanceResponseDTO toDTO(OvertimeBalance balance) {
        return new OvertimeBalanceResponseDTO(
                balance.getEmployee().getId(),
                balance.getEmployee().getName(),
                balance.getTotalMinutes(),
                WorkTimeCalculator.formatMinutes(balance.getTotalMinutes())
        );
    }

    private Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found"));
    }

    private Employee getEmployeeById(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found"));
    }
}

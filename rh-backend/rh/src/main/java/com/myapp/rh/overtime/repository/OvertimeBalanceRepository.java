package com.myapp.rh.overtime.repository;

import com.myapp.rh.overtime.entity.OvertimeBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OvertimeBalanceRepository extends
        JpaRepository<OvertimeBalance, UUID> {

    Optional<OvertimeBalance> findByEmployeeId(UUID employeeId);
}

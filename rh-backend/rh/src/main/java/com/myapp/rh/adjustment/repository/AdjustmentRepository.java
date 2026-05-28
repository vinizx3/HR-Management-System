package com.myapp.rh.adjustment.repository;

import com.myapp.rh.adjustment.entity.Adjustment;
import com.myapp.rh.adjustment.entity.AdjustmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdjustmentRepository extends JpaRepository<Adjustment, UUID> {

    List<Adjustment> findByEmployeeId(UUID employeeId);

    List<Adjustment> findByStatus(AdjustmentStatus status);
}

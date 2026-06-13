package com.myapp.rh.timeclock.repository;

import com.myapp.rh.timeclock.entity.TimeRecord;
import com.myapp.rh.timeclock.entity.TimeRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, UUID> {

    Optional<TimeRecord> findByEmployeeIdAndDateAndStatus(
    UUID employeeId,
    LocalDate date,
    TimeRecordStatus status
    );

    Optional<TimeRecord> findByEmployeeIdAndStatus(UUID employeeId, TimeRecordStatus status);

    List<TimeRecord> findByEmployeeId(UUID employeeId);
}

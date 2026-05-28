package com.myapp.rh.vacation.repository;

import com.myapp.rh.vacation.entity.VacationRequest;
import com.myapp.rh.vacation.entity.VacationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, UUID> {

    @Query("""
        SELECT vr
        FROM VacationRequest vr
        JOIN FETCH vr.employee
        WHERE vr.employee.id = :employeeId
        ORDER BY vr.requestedAt DESC
    """)
    List<VacationRequest> findByEmployeeId(UUID employeeId);

    @Query("""
        SELECT vr
        FROM VacationRequest vr
        JOIN FETCH vr.employee
        WHERE vr.vacationStatus = :vacationStatus
        ORDER BY vr.requestedAt DESC
    """)
    List<VacationRequest> findByVacationStatus(VacationStatus vacationStatus);

    @Query("""
    SELECT vr
    FROM VacationRequest vr
    JOIN FETCH vr.employee
    ORDER BY vr.requestedAt DESC
""")
    List<VacationRequest> findAllByOrderByRequestedAtDesc();

    @Query("""
    SELECT vr
    FROM VacationRequest vr
    JOIN FETCH vr.employee
    WHERE vr.id = :id
""")
    Optional<VacationRequest> findByIdWithEmployee(
            @Param("id")
            UUID id
    );

    @Query("""
    SELECT vr
    FROM VacationRequest vr
    JOIN FETCH vr.employee
    WHERE vr.vacationStatus = 'APPROVED'
    AND vr.startDate BETWEEN :today AND :limitDate
    ORDER BY vr.startDate ASC
""")
    List<VacationRequest> findExpiringVacations(
            @Param("today") LocalDate today,
            @Param("limitDate") LocalDate limitDate
    );
}

package com.myapp.rh.vacation.dto;

import com.myapp.rh.vacation.entity.VacationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VacationResponseDTO(

        UUID id,
        String employeeName,
        String startDate,
        String endDate,
        Integer vacationDays,
        VacationStatus vacationStatus,
        String requestedAt
) {}

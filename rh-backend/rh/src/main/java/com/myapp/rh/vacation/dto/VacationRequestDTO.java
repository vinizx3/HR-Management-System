package com.myapp.rh.vacation.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VacationRequestDTO(

        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDate startDate,

        @NotNull(message = "End date ir required")
        LocalDate endDate
) {}

package com.myapp.rh.overtime.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OvertimeCompensationRequestDTO(

        @NotNull(message = "Minutes is required")
        @Positive(message = "Minutes must be greater than zero")
        Integer minutes,

        @NotBlank(message = "Reason is required")
        String reason
) {
}

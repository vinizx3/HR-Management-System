package com.myapp.rh.adjustment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdjustmentRequestDTO(
        @NotNull(message = "Time record ID is required")
        UUID timeRecordId,

        @NotNull(message = "Clock in is required")
        LocalDateTime requestedClockIn,

        @NotNull(message = "Clock out is required")
        LocalDateTime requestedClockOut,

        @NotBlank(message = "Reason is required")
        String reason
) {
}

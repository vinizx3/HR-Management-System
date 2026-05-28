package com.myapp.rh.adjustment.dto;

import com.myapp.rh.adjustment.entity.AdjustmentStatus;

import java.util.UUID;

public record AdjustmentResponseDTO(
        UUID id,
        UUID timeRecordId,
        String employeeName,
        String requestedClockIn,
        String requestedClockOut,
        String reason,
        AdjustmentStatus status,
        String requestedAt
) {
}

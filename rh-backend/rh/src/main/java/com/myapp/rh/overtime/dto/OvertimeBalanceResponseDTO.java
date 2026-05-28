package com.myapp.rh.overtime.dto;

import java.util.UUID;

public record OvertimeBalanceResponseDTO(

        UUID employeeId,
        String employeeName,
        Integer totalMinutes,
        String formatedBalance
) {}

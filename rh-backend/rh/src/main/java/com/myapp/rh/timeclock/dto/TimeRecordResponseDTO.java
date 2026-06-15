package com.myapp.rh.timeclock.dto;

import com.myapp.rh.timeclock.entity.TimeRecordStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeRecordResponseDTO(

        UUID id,
        String employeeName,
        String employeeEmail,
        String date,
        String clockIn,
        String clockOut,
        Integer workedMinutes,
        String workedTime,
        Integer overtimeMinutes,
        String overtimeTime,
        TimeRecordStatus status
) {}

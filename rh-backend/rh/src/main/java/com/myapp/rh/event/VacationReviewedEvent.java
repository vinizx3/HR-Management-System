package com.myapp.rh.event;

import java.util.UUID;

public record VacationReviewedEvent(

        UUID id,
        String employeeName,
        String employeeEmail,
        String status
) {
}

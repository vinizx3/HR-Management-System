package com.myapp.rh.notification.dto;

import java.util.UUID;

public record NotificationResponseDTO(

        UUID id,
        String message,
        boolean read,
        String createdAt
) {
}

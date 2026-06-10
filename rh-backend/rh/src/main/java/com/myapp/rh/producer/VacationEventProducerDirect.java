package com.myapp.rh.producer;

import com.myapp.rh.event.VacationReviewedEvent;
import com.myapp.rh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class VacationEventProducerDirect implements VacationEventProducerPort {

    private final NotificationService notificationService;

    @Override
    public void sendVacationReviewedEvent(VacationReviewedEvent event) {
        log.info("Direct notification (prod profile) | employee={}", event.employeeName());
        notificationService.createFromVacationEvent(event);
    }
}   
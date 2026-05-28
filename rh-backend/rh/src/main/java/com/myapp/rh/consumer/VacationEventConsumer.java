package com.myapp.rh.consumer;

import com.myapp.rh.event.VacationReviewedEvent;
import com.myapp.rh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "vacation-reviewed-topic", groupId = "rh-group")

    public void consume(VacationReviewedEvent event) {

        if (event.status().equals("APPROVED")) {
            log.info("Vacation APPROVED | employee={} | vacationId={}",
                    event.employeeName(), event.id());
        } else {
            log.info("Vacation REJECTED | employee={} | vacationId={}",
                    event.employeeName(), event.id());

        }

        log.info("🔔 Event received: {}", event);

        notificationService.createFromVacationEvent(event);
    }
}

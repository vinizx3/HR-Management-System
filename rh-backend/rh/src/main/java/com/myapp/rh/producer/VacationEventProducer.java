package com.myapp.rh.producer;

import com.myapp.rh.event.VacationReviewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VacationEventProducer {

    private final KafkaTemplate<String, VacationReviewedEvent> kafkaTemplate;

    private static final String TOPIC = "vacation-reviewed-topic";

    public void sendVacationReviewedEvent(VacationReviewedEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}

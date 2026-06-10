package com.myapp.rh.producer;

import com.myapp.rh.event.VacationReviewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod")
@RequiredArgsConstructor
public class VacationEventProducer implements VacationEventProducerPort {

    private final KafkaTemplate<String, VacationReviewedEvent> kafkaTemplate;
    private static final String TOPIC = "vacation-reviewed-topic";

    @Override
    public void sendVacationReviewedEvent(VacationReviewedEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}

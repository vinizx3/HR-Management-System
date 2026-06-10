package com.myapp.rh.producer;

import com.myapp.rh.event.VacationReviewedEvent;

public interface VacationEventProducerPort {
    void sendVacationReviewedEvent(VacationReviewedEvent event);
}
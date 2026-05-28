package com.myapp.rh.notification.service;

import com.myapp.rh.event.VacationReviewedEvent;
import com.myapp.rh.notification.dto.NotificationResponseDTO;
import com.myapp.rh.notification.entity.Notification;
import com.myapp.rh.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public void createFromVacationEvent(VacationReviewedEvent event) {

        String message = event.status().equals("APPROVED")
                ? "Your vacation request has been approved! 🎉"
                : "Your vacation request has been rejected.";

        Notification notification = Notification.builder()
                .employeeEmail(event.employeeEmail())
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now(clock))
                .build();

        notificationRepository.save(notification);

        log.info("Notification created | email={} | status={}",
                event.employeeEmail(), event.status());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getMyNotifications(String email) {
        return notificationRepository
                .findByEmployeeEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(String email) {
        return notificationRepository
                .findByEmployeeEmailAndReadFalse(email)
                .size();
    }

    public void markAllAsRead(String email) {

        List<Notification> unread = notificationRepository
                .findByEmployeeEmailAndReadFalse(email);

        unread.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(unread);

        log.info("Notifications marked as read | email={} | count={}",
                email, unread.size());
    }

    private NotificationResponseDTO toDTO(Notification notification) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt().format(formatter)
        );
    }
}

package com.myapp.rh;

import com.myapp.rh.event.VacationReviewedEvent;
import com.myapp.rh.notification.dto.NotificationResponseDTO;
import com.myapp.rh.notification.entity.Notification;
import com.myapp.rh.notification.repository.NotificationRepository;
import com.myapp.rh.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private Clock clock;
    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(
                LocalDate.of(2026, 5, 17)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        notificationService = new NotificationService(notificationRepository, clock);
    }

    @Test
    void shouldCreateApprovedNotification() {

        VacationReviewedEvent event = new VacationReviewedEvent(
                UUID.randomUUID(),
                "John Doe",
                "john@test.com",
                "APPROVED"
        );

        notificationService.createFromVacationEvent(event);

        verify(notificationRepository).save(argThat(n ->
                n.getEmployeeEmail().equals("john@test.com") &&
                        n.getMessage().contains("approved") &&
                        !n.isRead()
        ));
    }

    @Test
    void shouldCreateRejectedNotification() {

        VacationReviewedEvent event = new VacationReviewedEvent(
                UUID.randomUUID(),
                "John Doe",
                "john@test.com",
                "REJECTED"
        );

        notificationService.createFromVacationEvent(event);

        verify(notificationRepository).save(argThat(n ->
                n.getMessage().contains("rejected")
        ));
    }

    @Test
    void shouldReturnMyNotifications() {

        Notification notification = new Notification();
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        notification.setEmployeeEmail("john@test.com");
        notification.setMessage("Your vacation was approved! 🎉");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now(clock));

        when(notificationRepository
                .findByEmployeeEmailOrderByCreatedAtDesc("john@test.com"))
                .thenReturn(List.of(notification));

        List<NotificationResponseDTO> result =
                notificationService.getMyNotifications("john@test.com");

        assertEquals(1, result.size());
        assertFalse(result.get(0).read());
    }

    @Test
    void shouldCountUnreadNotifications() {

        Notification unread = new Notification();
        unread.setRead(false);

        when(notificationRepository
                .findByEmployeeEmailAndReadFalse("john@test.com"))
                .thenReturn(List.of(unread, unread));

        long count = notificationService.countUnread("john@test.com");

        assertEquals(2, count);
    }

    @Test
    void shouldMarkAllNotificationsAsRead() {

        Notification n1 = new Notification();
        n1.setRead(false);

        Notification n2 = new Notification();
        n2.setRead(false);

        when(notificationRepository
                .findByEmployeeEmailAndReadFalse("john@test.com"))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead("john@test.com");

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }
}

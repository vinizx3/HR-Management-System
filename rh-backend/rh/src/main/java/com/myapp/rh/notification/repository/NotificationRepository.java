package com.myapp.rh.notification.repository;

import com.myapp.rh.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByEmployeeEmailOrderByCreatedAtDesc(
            String email);

    List<Notification> findByEmployeeEmailAndReadFalse(
            String employeeEmail);
}

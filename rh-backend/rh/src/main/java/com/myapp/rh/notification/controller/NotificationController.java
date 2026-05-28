package com.myapp.rh.notification.controller;

import com.myapp.rh.notification.dto.NotificationResponseDTO;
import com.myapp.rh.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Employee notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "My notifications", description = "Returns all notifications for authenticated employee")
    @ApiResponse(responseCode = "200", description = "Notifications returned successfully")
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
            Authentication authentication) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(authentication.getName()));
    }

    @Operation(summary = "Unread count", description = "Returns count of unread notifications")
    @ApiResponse(responseCode = "200", description = "Count returned successfully")
    @GetMapping("/me/unread-count")
    public ResponseEntity<Long> countUnread(
            Authentication authentication) {
        return ResponseEntity.ok(
                notificationService.countUnread(authentication.getName()));
    }

    @Operation(summary = "Mark all as read", description = "Marks all notifications as read")
    @ApiResponse(responseCode = "204", description = "All notifications marked as read")
    @PatchMapping("/me/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

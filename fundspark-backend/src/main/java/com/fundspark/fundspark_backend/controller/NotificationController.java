package com.fundspark.fundspark_backend.controller;

import com.fundspark.fundspark_backend.model.Notification;
import com.fundspark.fundspark_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // GET /api/notifications/user/{userId}   -> powers Notifications pages (all 3 roles)
    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(@PathVariable Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // PUT /api/notifications/{id}/read   -> mark a single notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Integer id) {
        return notificationRepository.findById(id).map(n -> {
            n.setIsRead(true);
            return ResponseEntity.ok(notificationRepository.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/notifications/user/{userId}/read-all   -> "Mark all as read" button
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllRead(@PathVariable Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
        return ResponseEntity.ok("All notifications marked as read");
    }
}

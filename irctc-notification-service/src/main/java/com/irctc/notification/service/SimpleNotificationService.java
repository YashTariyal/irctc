package com.irctc.notification.service;

import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.repository.SimpleNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimpleNotificationService {

    @Autowired
    private SimpleNotificationRepository notificationRepository;

    public List<SimpleNotification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public SimpleNotification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    public List<SimpleNotification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<SimpleNotification> getNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    public SimpleNotification createNotification(SimpleNotification notification) {
        notification.setSentTime(LocalDateTime.now());
        notification.setStatus("SENT");
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public SimpleNotification updateNotification(Long id, SimpleNotification notificationDetails) {
        SimpleNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setUserId(notificationDetails.getUserId());
        notification.setType(notificationDetails.getType());
        notification.setSubject(notificationDetails.getSubject());
        notification.setMessage(notificationDetails.getMessage());
        notification.setStatus(notificationDetails.getStatus());

        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}

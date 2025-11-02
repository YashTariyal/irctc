package com.irctc.notification.service;

import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.repository.SimpleNotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                .orElseThrow(() -> new com.irctc.notification.exception.EntityNotFoundException("Notification", id));
    }

    public List<SimpleNotification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<SimpleNotification> getNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    public List<SimpleNotification> getRecentNotificationsByUserId(Long userId, int limit) {
        int pageSize = Math.max(1, Math.min(limit, 100));
        Pageable pageable = PageRequest.of(0, pageSize);
        return notificationRepository.findByUserIdOrderBySentTimeDesc(userId, pageable).getContent();
    }

    public SimpleNotification createNotification(SimpleNotification notification) {
        notification.setSentTime(LocalDateTime.now());
        notification.setStatus("SENT");
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public SimpleNotification updateNotification(Long id, SimpleNotification notificationDetails) {
        SimpleNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new com.irctc.notification.exception.EntityNotFoundException("Notification", id));

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

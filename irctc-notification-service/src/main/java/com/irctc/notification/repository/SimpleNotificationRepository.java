package com.irctc.notification.repository;

import com.irctc.notification.entity.SimpleNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SimpleNotificationRepository extends JpaRepository<SimpleNotification, Long> {
    List<SimpleNotification> findByUserId(Long userId);
    List<SimpleNotification> findByType(String type);
}

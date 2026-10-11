package com.campus.placement.repository;

import com.campus.placement.entity.Notification;
import com.campus.placement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrUserIsNullOrderByCreatedAtDesc(User user);
}

package com.campus.placement.controller;

import com.campus.placement.dto.Dtos.NotificationView;
import com.campus.placement.entity.Notification;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.NotificationRepository;
import com.campus.placement.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repo;
    private final CurrentUser current;

    public NotificationController(NotificationRepository repo, CurrentUser current) {
        this.repo = repo; this.current = current;
    }

    @GetMapping
    public List<NotificationView> mine() {
        return repo.findByUserOrUserIsNullOrderByCreatedAtDesc(current.user()).stream()
                .map(n -> new NotificationView(n.getId(), n.getTitle(), n.getMessage(), n.getCreatedAt(),
                        n.isReadFlag(), n.getUser() == null))
                .toList();
    }

    @PutMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        Notification n = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Not found"));
        if (n.getUser() != null && n.getUser().getId().equals(current.user().getId())) {
            n.setReadFlag(true);
            repo.save(n);
        }
    }
}

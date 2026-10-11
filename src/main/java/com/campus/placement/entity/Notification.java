package com.campus.placement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** A notification for one user, or an announcement for everyone when user is null. */
@Entity
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String title;
    @Column(length = 1500)
    private String message;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean readFlag = false;
}

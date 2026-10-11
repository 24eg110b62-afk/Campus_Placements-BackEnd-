package com.campus.placement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "job_id"}))
@Getter @Setter @NoArgsConstructor
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private StudentProfile student;
    @ManyToOne(optional = false)
    private Job job;
    @Enumerated(EnumType.STRING)
    private AppStatus status = AppStatus.APPLIED;
    private LocalDateTime appliedAt = LocalDateTime.now();
    private LocalDateTime interviewAt;
    private String interviewVenue;
    private String remarks;
}

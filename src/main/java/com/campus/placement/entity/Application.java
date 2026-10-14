package com.campus.placement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "job_id"}))
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

    public Application() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentProfile getStudent() { return student; }
    public void setStudent(StudentProfile student) { this.student = student; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public AppStatus getStatus() { return status; }
    public void setStatus(AppStatus status) { this.status = status; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getInterviewAt() { return interviewAt; }
    public void setInterviewAt(LocalDateTime interviewAt) { this.interviewAt = interviewAt; }

    public String getInterviewVenue() { return interviewVenue; }
    public void setInterviewVenue(String interviewVenue) { this.interviewVenue = interviewVenue; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

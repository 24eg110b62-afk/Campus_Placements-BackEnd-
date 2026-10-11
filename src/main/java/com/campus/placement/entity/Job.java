package com.campus.placement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Company company;
    private String title;
    @Column(length = 3000)
    private String description;
    private String location;
    private double packageLpa;
    private double minCgpa;
    private int maxBacklogs;
    /** Comma separated branches. Empty means every branch is allowed. */
    private String allowedBranches;
    /** Comma separated skills. A student must have all of them. */
    private String requiredSkills;
    private LocalDate deadline;
    private LocalDate driveDate;
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.OPEN;
}

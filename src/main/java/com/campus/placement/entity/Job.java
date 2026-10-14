package com.campus.placement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
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

    public Job() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getPackageLpa() { return packageLpa; }
    public void setPackageLpa(double packageLpa) { this.packageLpa = packageLpa; }

    public double getMinCgpa() { return minCgpa; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }

    public int getMaxBacklogs() { return maxBacklogs; }
    public void setMaxBacklogs(int maxBacklogs) { this.maxBacklogs = maxBacklogs; }

    public String getAllowedBranches() { return allowedBranches; }
    public void setAllowedBranches(String allowedBranches) { this.allowedBranches = allowedBranches; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public LocalDate getDriveDate() { return driveDate; }
    public void setDriveDate(LocalDate driveDate) { this.driveDate = driveDate; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
}

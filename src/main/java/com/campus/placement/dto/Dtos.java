package com.campus.placement.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    // ---- auth ----
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, String role, String name, String email) {}

    public record StudentRegistration(
            @NotBlank String name, @Email @NotBlank String email, @Size(min = 6) String password,
            String rollNo, String branch, @PositiveOrZero double cgpa, @PositiveOrZero int backlogs,
            int graduationYear, String skills, String phone) {}

    public record CompanyRegistration(
            @NotBlank String name, @Email @NotBlank String email, @Size(min = 6) String password,
            @NotBlank String companyName, String industry, String website, String description) {}

    // ---- student ----
    public record ProfileUpdate(String rollNo, String branch, @PositiveOrZero double cgpa,
                                @PositiveOrZero int backlogs, int graduationYear, String phone, String skills) {}

    public record ProfileView(Long id, String name, String email, String rollNo, String branch, double cgpa,
                              int backlogs, int graduationYear, String phone, String skills, String resumeFile) {}

    // ---- company ----
    public record CompanyView(Long id, String name, String email, String industry, String website,
                              String description, boolean approved) {}

    // ---- jobs ----
    public record JobRequest(@NotBlank String title, String description, String location,
                             @PositiveOrZero double packageLpa, @PositiveOrZero double minCgpa,
                             @PositiveOrZero int maxBacklogs, String allowedBranches, String requiredSkills,
                             LocalDate deadline, LocalDate driveDate) {}

    public record DriveUpdate(LocalDate driveDate, String status) {}

    public record JobView(Long id, String title, String description, String location, double packageLpa,
                          double minCgpa, int maxBacklogs, String allowedBranches, String requiredSkills,
                          LocalDate deadline, LocalDate driveDate, String status,
                          Long companyId, String companyName,
                          boolean eligible, List<String> issues, boolean applied) {}

    // ---- applications ----
    public record ApplicationView(Long id, Long jobId, String jobTitle, String companyName,
                                  Long studentId, String studentName, String rollNo, String branch,
                                  double cgpa, String skills, String resumeFile, String status,
                                  LocalDateTime appliedAt, LocalDateTime interviewAt,
                                  String interviewVenue, String remarks) {}

    public record StatusUpdate(@NotBlank String status, String remarks) {}
    public record InterviewRequest(@NotNull LocalDateTime interviewAt, @NotBlank String venue) {}

    // ---- notifications ----
    public record AnnouncementRequest(@NotBlank String title, @NotBlank String message) {}
    public record NotificationView(Long id, String title, String message, LocalDateTime createdAt,
                                   boolean read, boolean broadcast) {}
}

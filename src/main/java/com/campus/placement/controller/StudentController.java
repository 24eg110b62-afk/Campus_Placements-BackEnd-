package com.campus.placement.controller;

import com.campus.placement.dto.Dtos.*;
import com.campus.placement.entity.*;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.*;
import com.campus.placement.security.CurrentUser;
import com.campus.placement.service.PlacementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final CurrentUser current;
    private final StudentProfileRepository students;
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final PlacementService service;
    private final Path uploadDir;

    public StudentController(CurrentUser current, StudentProfileRepository students, JobRepository jobs,
                             ApplicationRepository applications, PlacementService service,
                             @Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.current = current; this.students = students; this.jobs = jobs;
        this.applications = applications; this.service = service;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    @GetMapping("/profile")
    public ProfileView profile() { return service.profileView(current.student()); }

    @PutMapping("/profile")
    public ProfileView update(@Valid @RequestBody ProfileUpdate r) {
        StudentProfile p = current.student();
        p.setRollNo(r.rollNo());
        p.setBranch(r.branch());
        p.setCgpa(r.cgpa());
        p.setBacklogs(r.backlogs());
        p.setGraduationYear(r.graduationYear());
        p.setPhone(r.phone());
        p.setSkills(r.skills());
        return service.profileView(students.save(p));
    }

    @PostMapping("/resume")
    public ProfileView uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (file.isEmpty() || !original.endsWith(".pdf"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please upload your resume as a PDF file");
        StudentProfile p = current.student();
        String name = UUID.randomUUID() + ".pdf";
        Files.copy(file.getInputStream(), uploadDir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        p.setResumeFile(name);
        return service.profileView(students.save(p));
    }

    @GetMapping("/jobs")
    public List<JobView> jobs() {
        StudentProfile me = current.student();
        return jobs.findByStatusOrderByIdDesc(JobStatus.OPEN).stream().map(j -> service.jobView(j, me)).toList();
    }

    @PostMapping("/jobs/{id}/apply")
    public ApplicationView apply(@PathVariable Long id) {
        return service.appView(service.apply(current.student(), id));
    }

    @GetMapping("/applications")
    public List<ApplicationView> myApplications() {
        return applications.findByStudentOrderByAppliedAtDesc(current.student()).stream().map(service::appView).toList();
    }
}

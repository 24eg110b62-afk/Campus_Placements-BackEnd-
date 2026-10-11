package com.campus.placement.controller;

import com.campus.placement.dto.Dtos.*;
import com.campus.placement.entity.*;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.*;
import com.campus.placement.service.PlacementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final StudentProfileRepository students;
    private final CompanyRepository companies;
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final PlacementService service;

    public AdminController(StudentProfileRepository students, CompanyRepository companies, JobRepository jobs,
                           ApplicationRepository applications, PlacementService service) {
        this.students = students; this.companies = companies; this.jobs = jobs;
        this.applications = applications; this.service = service;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return service.stats(); }

    @GetMapping("/students")
    public List<ProfileView> students() { return students.findAll().stream().map(service::profileView).toList(); }

    @GetMapping("/companies")
    public List<CompanyView> companies() { return companies.findAll().stream().map(service::companyView).toList(); }

    @PutMapping("/companies/{id}/approve")
    public CompanyView approve(@PathVariable Long id, @RequestParam boolean approved) {
        Company c = companies.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Company not found"));
        c.setApproved(approved);
        companies.save(c);
        service.notifyUser(c.getUser(), approved ? "Company approved" : "Company approval withdrawn",
                approved ? "You can now post placement drives." : "Your company can no longer post drives.");
        return service.companyView(c);
    }

    @GetMapping("/jobs")
    public List<JobView> jobs() { return jobs.findAllByOrderByIdDesc().stream().map(j -> service.jobView(j, null)).toList(); }

    @PutMapping("/jobs/{id}/drive")
    public JobView updateDrive(@PathVariable Long id, @RequestBody DriveUpdate r) {
        Job j = service.getJob(id);
        if (r.driveDate() != null) j.setDriveDate(r.driveDate());
        if (r.status() != null) j.setStatus(JobStatus.valueOf(r.status()));
        jobs.save(j);
        if (r.driveDate() != null)
            service.announce("Drive scheduled: " + j.getCompany().getName(),
                    j.getTitle() + " drive is scheduled for " + r.driveDate() + ".");
        return service.jobView(j, null);
    }

    @GetMapping("/applications")
    public List<ApplicationView> applications() {
        return applications.findAllByOrderByAppliedAtDesc().stream().map(service::appView).toList();
    }

    @PutMapping("/applications/{id}/status")
    public ApplicationView status(@PathVariable Long id, @Valid @RequestBody StatusUpdate r) {
        return service.appView(service.updateStatus(service.getApplication(id), r.status(), r.remarks()));
    }

    @PutMapping("/applications/{id}/interview")
    public ApplicationView interview(@PathVariable Long id, @Valid @RequestBody InterviewRequest r) {
        return service.appView(service.scheduleInterview(service.getApplication(id), r.interviewAt(), r.venue()));
    }

    @PostMapping("/announcements")
    public Map<String, String> announce(@Valid @RequestBody AnnouncementRequest r) {
        service.announce(r.title(), r.message());
        return Map.of("message", "Announcement sent");
    }

    @GetMapping("/reports/placements.csv")
    public ResponseEntity<byte[]> report() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=placements.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(service.placementsCsv().getBytes(StandardCharsets.UTF_8));
    }
}

package com.campus.placement.controller;

import com.campus.placement.dto.Dtos.*;
import com.campus.placement.entity.*;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.*;
import com.campus.placement.security.CurrentUser;
import com.campus.placement.service.PlacementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
public class CompanyController {
    private final CurrentUser current;
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final PlacementService service;

    public CompanyController(CurrentUser current, JobRepository jobs, ApplicationRepository applications,
                             PlacementService service) {
        this.current = current; this.jobs = jobs; this.applications = applications; this.service = service;
    }

    @GetMapping("/profile")
    public CompanyView profile() { return service.companyView(current.company()); }

    @PostMapping("/jobs")
    public JobView create(@Valid @RequestBody JobRequest r) {
        return service.jobView(service.createJob(current.company(), r), null);
    }

    @GetMapping("/jobs")
    public List<JobView> myJobs() {
        return jobs.findByCompanyOrderByIdDesc(current.company()).stream().map(j -> service.jobView(j, null)).toList();
    }

    @PatchMapping("/jobs/{id}/close")
    public JobView close(@PathVariable Long id) {
        Job j = ownJob(id);
        j.setStatus(JobStatus.CLOSED);
        return service.jobView(jobs.save(j), null);
    }

    @GetMapping("/jobs/{id}/applications")
    public List<ApplicationView> applicants(@PathVariable Long id) {
        return applications.findByJobOrderByAppliedAtDesc(ownJob(id)).stream().map(service::appView).toList();
    }

    @PutMapping("/applications/{id}/status")
    public ApplicationView status(@PathVariable Long id, @Valid @RequestBody StatusUpdate r) {
        return service.appView(service.updateStatus(ownApplication(id), r.status(), r.remarks()));
    }

    @PutMapping("/applications/{id}/interview")
    public ApplicationView interview(@PathVariable Long id, @Valid @RequestBody InterviewRequest r) {
        return service.appView(service.scheduleInterview(ownApplication(id), r.interviewAt(), r.venue()));
    }

    private Job ownJob(Long id) {
        Job j = service.getJob(id);
        if (!j.getCompany().getId().equals(current.company().getId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "This drive belongs to another company");
        return j;
    }

    private Application ownApplication(Long id) {
        Application a = service.getApplication(id);
        if (!a.getJob().getCompany().getId().equals(current.company().getId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "This application belongs to another company");
        return a;
    }
}

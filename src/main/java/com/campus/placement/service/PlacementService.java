package com.campus.placement.service;

import com.campus.placement.dto.Dtos.*;
import com.campus.placement.entity.*;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlacementService {
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final StudentProfileRepository students;
    private final CompanyRepository companies;
    private final NotificationRepository notifications;

    public PlacementService(JobRepository jobs, ApplicationRepository applications, StudentProfileRepository students,
                            CompanyRepository companies, NotificationRepository notifications) {
        this.jobs = jobs; this.applications = applications; this.students = students;
        this.companies = companies; this.notifications = notifications;
    }

    // ------------------------------------------------------------------ eligibility
    private static Set<String> tokens(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(",")).map(s -> s.trim().toLowerCase()).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Returns the reasons a student is not eligible. Empty list means eligible. */
    public List<String> eligibilityIssues(StudentProfile s, Job j) {
        List<String> issues = new ArrayList<>();
        if (s.getCgpa() < j.getMinCgpa()) issues.add("Minimum CGPA is " + j.getMinCgpa());
        if (s.getBacklogs() > j.getMaxBacklogs()) issues.add("Maximum " + j.getMaxBacklogs() + " backlogs allowed");
        Set<String> branches = tokens(j.getAllowedBranches());
        if (!branches.isEmpty() && (s.getBranch() == null || !branches.contains(s.getBranch().trim().toLowerCase())))
            issues.add("Open only to: " + j.getAllowedBranches());
        Set<String> have = tokens(s.getSkills());
        List<String> missing = tokens(j.getRequiredSkills()).stream().filter(x -> !have.contains(x)).toList();
        if (!missing.isEmpty()) issues.add("Missing skills: " + String.join(", ", missing));
        return issues;
    }

    // ------------------------------------------------------------------ views
    public JobView jobView(Job j, StudentProfile viewer) {
        List<String> issues = viewer == null ? List.of() : eligibilityIssues(viewer, j);
        boolean applied = viewer != null && applications.existsByStudentAndJob(viewer, j);
        return new JobView(j.getId(), j.getTitle(), j.getDescription(), j.getLocation(), j.getPackageLpa(),
                j.getMinCgpa(), j.getMaxBacklogs(), j.getAllowedBranches(), j.getRequiredSkills(),
                j.getDeadline(), j.getDriveDate(), j.getStatus().name(),
                j.getCompany().getId(), j.getCompany().getName(), issues.isEmpty(), issues, applied);
    }

    public ApplicationView appView(Application a) {
        StudentProfile s = a.getStudent();
        Job j = a.getJob();
        return new ApplicationView(a.getId(), j.getId(), j.getTitle(), j.getCompany().getName(),
                s.getId(), s.getUser().getName(), s.getRollNo(), s.getBranch(), s.getCgpa(), s.getSkills(),
                s.getResumeFile(), a.getStatus().name(), a.getAppliedAt(), a.getInterviewAt(),
                a.getInterviewVenue(), a.getRemarks());
    }

    public ProfileView profileView(StudentProfile s) {
        return new ProfileView(s.getId(), s.getUser().getName(), s.getUser().getEmail(), s.getRollNo(), s.getBranch(),
                s.getCgpa(), s.getBacklogs(), s.getGraduationYear(), s.getPhone(), s.getSkills(), s.getResumeFile());
    }

    public CompanyView companyView(Company c) {
        return new CompanyView(c.getId(), c.getName(), c.getUser().getEmail(), c.getIndustry(), c.getWebsite(),
                c.getDescription(), c.isApproved());
    }

    // ------------------------------------------------------------------ jobs
    @Transactional
    public Job createJob(Company company, JobRequest r) {
        if (!company.isApproved())
            throw new ApiException(HttpStatus.FORBIDDEN, "Your company is awaiting approval from the placement office");
        Job j = new Job();
        j.setCompany(company);
        j.setTitle(r.title());
        j.setDescription(r.description());
        j.setLocation(r.location());
        j.setPackageLpa(r.packageLpa());
        j.setMinCgpa(r.minCgpa());
        j.setMaxBacklogs(r.maxBacklogs());
        j.setAllowedBranches(r.allowedBranches());
        j.setRequiredSkills(r.requiredSkills());
        j.setDeadline(r.deadline());
        j.setDriveDate(r.driveDate());
        j.setStatus(JobStatus.OPEN);
        Job saved = jobs.save(j);
        announce("New opening: " + saved.getTitle(),
                company.getName() + " is hiring. Check your eligibility and apply" +
                        (saved.getDeadline() != null ? " before " + saved.getDeadline() : "") + ".");
        return saved;
    }

    public Job getJob(Long id) {
        return jobs.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    // ------------------------------------------------------------------ applications
    @Transactional
    public Application apply(StudentProfile student, Long jobId) {
        Job job = getJob(jobId);
        if (job.getStatus() != JobStatus.OPEN)
            throw new ApiException(HttpStatus.BAD_REQUEST, "This drive is closed");
        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "The application deadline has passed");
        if (student.getResumeFile() == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Upload your resume before applying");
        List<String> issues = eligibilityIssues(student, job);
        if (!issues.isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not eligible: " + String.join("; ", issues));
        if (applications.existsByStudentAndJob(student, job))
            throw new ApiException(HttpStatus.CONFLICT, "You have already applied to this drive");
        Application a = new Application();
        a.setStudent(student);
        a.setJob(job);
        Application saved = applications.save(a);
        notifyUser(job.getCompany().getUser(), "New application",
                student.getUser().getName() + " applied for " + job.getTitle() + ".");
        return saved;
    }

    public Application getApplication(Long id) {
        return applications.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    @Transactional
    public Application updateStatus(Application a, String status, String remarks) {
        AppStatus s;
        try { s = AppStatus.valueOf(status); }
        catch (IllegalArgumentException e) { throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown status " + status); }
        a.setStatus(s);
        if (remarks != null && !remarks.isBlank()) a.setRemarks(remarks);
        applications.save(a);
        notifyUser(a.getStudent().getUser(), "Application update",
                "Your application for " + a.getJob().getTitle() + " at " + a.getJob().getCompany().getName() +
                        " is now " + s.name().replace('_', ' ').toLowerCase() + ".");
        return a;
    }

    @Transactional
    public Application scheduleInterview(Application a, LocalDateTime at, String venue) {
        a.setInterviewAt(at);
        a.setInterviewVenue(venue);
        a.setStatus(AppStatus.INTERVIEW_SCHEDULED);
        applications.save(a);
        notifyUser(a.getStudent().getUser(), "Interview scheduled",
                a.getJob().getCompany().getName() + " has scheduled your interview for " + a.getJob().getTitle() +
                        " on " + at.toLocalDate() + " at " + at.toLocalTime() + ", venue: " + venue + ".");
        return a;
    }

    // ------------------------------------------------------------------ notifications
    @Transactional
    public void notifyUser(User user, String title, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        notifications.save(n);
    }

    @Transactional
    public void announce(String title, String message) { notifyUser(null, title, message); }

    // ------------------------------------------------------------------ reports
    public Map<String, Object> stats() {
        List<Application> all = applications.findAllByOrderByAppliedAtDesc();
        List<Application> selected = all.stream().filter(a -> a.getStatus() == AppStatus.SELECTED).toList();
        Set<Long> placedIds = selected.stream().map(a -> a.getStudent().getId()).collect(Collectors.toSet());
        List<StudentProfile> allStudents = students.findAll();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (AppStatus s : AppStatus.values()) byStatus.put(s.name(), 0L);
        all.forEach(a -> byStatus.merge(a.getStatus().name(), 1L, Long::sum));

        Map<String, long[]> branch = new TreeMap<>();
        for (StudentProfile s : allStudents) {
            String b = s.getBranch() == null || s.getBranch().isBlank() ? "Unspecified" : s.getBranch();
            long[] v = branch.computeIfAbsent(b, k -> new long[2]);
            v[0]++;
            if (placedIds.contains(s.getId())) v[1]++;
        }
        List<Map<String, Object>> branchWise = branch.entrySet().stream()
                .map(e -> Map.<String, Object>of("branch", e.getKey(), "total", e.getValue()[0], "placed", e.getValue()[1]))
                .toList();

        DoubleSummaryStatistics pkg = selected.stream().mapToDouble(a -> a.getJob().getPackageLpa()).summaryStatistics();
        long totalStudents = allStudents.size();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalStudents", totalStudents);
        out.put("totalCompanies", companies.count());
        out.put("approvedCompanies", companies.findAll().stream().filter(Company::isApproved).count());
        out.put("totalDrives", jobs.count());
        out.put("totalApplications", all.size());
        out.put("placedStudents", placedIds.size());
        out.put("placementRate", totalStudents == 0 ? 0 : Math.round(placedIds.size() * 1000.0 / totalStudents) / 10.0);
        out.put("averagePackage", selected.isEmpty() ? 0 : Math.round(pkg.getAverage() * 100.0) / 100.0);
        out.put("highestPackage", selected.isEmpty() ? 0 : pkg.getMax());
        out.put("byStatus", byStatus);
        out.put("branchWise", branchWise);
        return out;
    }

    public String placementsCsv() {
        StringBuilder sb = new StringBuilder("Student,Roll No,Branch,CGPA,Company,Role,Package (LPA)\n");
        applications.findAllByOrderByAppliedAtDesc().stream()
                .filter(a -> a.getStatus() == AppStatus.SELECTED)
                .forEach(a -> sb.append(String.join(",",
                        csv(a.getStudent().getUser().getName()), csv(a.getStudent().getRollNo()),
                        csv(a.getStudent().getBranch()), String.valueOf(a.getStudent().getCgpa()),
                        csv(a.getJob().getCompany().getName()), csv(a.getJob().getTitle()),
                        String.valueOf(a.getJob().getPackageLpa()))).append('\n'));
        return sb.toString();
    }

    private static String csv(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}

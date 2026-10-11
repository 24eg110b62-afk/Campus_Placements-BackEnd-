package com.campus.placement.repository;

import com.campus.placement.entity.Company;
import com.campus.placement.entity.Job;
import com.campus.placement.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByCompanyOrderByIdDesc(Company company);
    List<Job> findByStatusOrderByIdDesc(JobStatus status);
    List<Job> findAllByOrderByIdDesc();
}

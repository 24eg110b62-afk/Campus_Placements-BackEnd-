package com.campus.placement.repository;

import com.campus.placement.entity.Application;
import com.campus.placement.entity.Job;
import com.campus.placement.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentAndJob(StudentProfile student, Job job);
    List<Application> findByStudentOrderByAppliedAtDesc(StudentProfile student);
    List<Application> findByJobOrderByAppliedAtDesc(Job job);
    List<Application> findAllByOrderByAppliedAtDesc();
}

package com.campus.placement.security;

import com.campus.placement.entity.*;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the logged-in user (and their student/company profile) from the JWT. */
@Component
public class CurrentUser {
    private final UserRepository users;
    private final StudentProfileRepository students;
    private final CompanyRepository companies;

    public CurrentUser(UserRepository users, StudentProfileRepository students, CompanyRepository companies) {
        this.users = users; this.students = students; this.companies = companies;
    }

    public User user() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public StudentProfile student() {
        return students.findByUser(user()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student profile not found"));
    }

    public Company company() {
        return companies.findByUser(user()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Company profile not found"));
    }
}

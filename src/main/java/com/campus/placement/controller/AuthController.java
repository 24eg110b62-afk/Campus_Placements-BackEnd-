package com.campus.placement.controller;

import com.campus.placement.dto.Dtos.*;
import com.campus.placement.entity.*;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.*;
import com.campus.placement.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final StudentProfileRepository students;
    private final CompanyRepository companies;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthController(UserRepository users, StudentProfileRepository students, CompanyRepository companies,
                          PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users; this.students = students; this.companies = companies;
        this.encoder = encoder; this.jwt = jwt;
    }

    private User newUser(String name, String email, String password, Role role) {
        if (users.existsByEmail(email.toLowerCase()))
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        User u = new User();
        u.setName(name);
        u.setEmail(email.toLowerCase());
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        return users.save(u);
    }

    private AuthResponse response(User u) {
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getRole().name(), u.getName(), u.getEmail());
    }

    @PostMapping("/register/student")
    @Transactional
    public AuthResponse registerStudent(@Valid @RequestBody StudentRegistration r) {
        User u = newUser(r.name(), r.email(), r.password(), Role.STUDENT);
        StudentProfile p = new StudentProfile();
        p.setUser(u);
        p.setRollNo(r.rollNo());
        p.setBranch(r.branch());
        p.setCgpa(r.cgpa());
        p.setBacklogs(r.backlogs());
        p.setGraduationYear(r.graduationYear());
        p.setSkills(r.skills());
        p.setPhone(r.phone());
        students.save(p);
        return response(u);
    }

    @PostMapping("/register/company")
    @Transactional
    public AuthResponse registerCompany(@Valid @RequestBody CompanyRegistration r) {
        User u = newUser(r.name(), r.email(), r.password(), Role.COMPANY);
        Company c = new Company();
        c.setUser(u);
        c.setName(r.companyName());
        c.setIndustry(r.industry());
        c.setWebsite(r.website());
        c.setDescription(r.description());
        c.setApproved(false);
        companies.save(c);
        return response(u);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        User u = users.findByEmail(r.email().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect email or password"));
        return response(u);
    }
}

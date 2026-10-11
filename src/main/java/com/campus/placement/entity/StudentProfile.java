package com.campus.placement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class StudentProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    private User user;
    private String rollNo;
    private String branch;
    private double cgpa;
    private int backlogs;
    private int graduationYear;
    private String phone;
    /** Comma separated, e.g. "Java, React, SQL" */
    @Column(length = 1000)
    private String skills;
    private String resumeFile;
}

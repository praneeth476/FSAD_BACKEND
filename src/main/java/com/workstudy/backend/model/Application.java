package com.workstudy.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Job job;

    private String status;

    @Column(name="resume_path")
    private String resumePath;

    @Transient
    private Integer matchScore;

    @Transient
    private String missingSkills;

    public Long getId() { return id; }

    public Student getStudent() { return student; }

    public Job getJob() { return job; }

    public String getStatus() { return status; }

    public String getResumePath() { return resumePath; }

    public void setId(Long id) { this.id = id; }

    public void setStudent(Student student) { this.student = student; }

    public void setJob(Job job) { this.job = job; }

    public void setStatus(String status) { this.status = status; }

    public void setResumePath(String resumePath) { this.resumePath = resumePath; }

    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }

    public String getMissingSkills() { return missingSkills; }
    public void setMissingSkills(String missingSkills) { this.missingSkills = missingSkills; }
}

package com.workstudy.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "work_hour_records")
public class WorkHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double hours;

    private String status = "PENDING"; // PENDING, APPROVED, PAID
    private String date;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Job job;

    public Long getId() {
        return id;
    }

    public Double getHours() {
        return hours;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Student getStudent() {
        return student;
    }

    public Job getJob() {
        return job;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHours(Double hours) {
        this.hours = hours;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}

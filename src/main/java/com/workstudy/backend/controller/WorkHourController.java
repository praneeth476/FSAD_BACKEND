package com.workstudy.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.workstudy.backend.model.Job;
import com.workstudy.backend.model.Student;
import com.workstudy.backend.model.WorkHour;
import com.workstudy.backend.repository.ApplicationRepository;
import com.workstudy.backend.repository.JobRepository;
import com.workstudy.backend.repository.StudentRepository;
import com.workstudy.backend.repository.WorkHourRepository;
import com.workstudy.backend.service.PaymentService;
import java.util.Date;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/api/hours")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class WorkHourController {

    @Autowired
    private WorkHourRepository workHourRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public WorkHour addHours(@RequestParam Long studentId,
                             @RequestParam Long jobId,
                             @RequestParam Double hours) {
        System.out.println("Processing addHours for Student: " + studentId + " Job: " + jobId + " Hours: " + hours);
        try {
            boolean approved = applicationRepository
                .existsByStudentIdAndJobIdAndStatus(studentId, jobId, "APPROVED");
            
            System.out.println("Is Approved? " + approved);

            if(!approved){
                throw new RuntimeException("Job not approved");
            }

            Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
            Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));

            WorkHour wh = new WorkHour();
            wh.setStudent(student);
            wh.setJob(job);
            wh.setHours(hours);
            wh.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            wh.setStatus("PENDING");

            return workHourRepository.save(wh);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error inside addHours: " + e.getMessage(), e);
        }
    }

    @GetMapping
    public List<WorkHour> getAllHours() {
        return workHourRepository.findAll();
    }

    @GetMapping("/student/{id}")
    public List<WorkHour> getStudentHours(@PathVariable Long id){
        return workHourRepository.findByStudentId(id);
    }

    @PutMapping("/{id}/approve")
    public WorkHour approveHours(@PathVariable Long id) {
        WorkHour wh = workHourRepository.findById(id).orElseThrow(() -> new RuntimeException("WorkHour not found"));
        wh.setStatus("APPROVED");
        return workHourRepository.save(wh);
    }

    @PostMapping("/{id}/pay")
    public Object payHours(@PathVariable Long id) {
        WorkHour wh = workHourRepository.findById(id).orElseThrow(() -> new RuntimeException("WorkHour not found"));
        if (!"APPROVED".equals(wh.getStatus())) {
            throw new RuntimeException("Cannot pay for unapproved hours");
        }

        Double amount = wh.getHours() * wh.getJob().getHourlyRate();
        String txnId = paymentService.processPayment(amount, wh.getStudent().getId());

        wh.setStatus("PAID");
        workHourRepository.save(wh);
        
        // Return a response map
        return java.util.Map.of("message", "Payment processing simulated successfully", "transactionId", txnId, "amount", amount);
    }
}

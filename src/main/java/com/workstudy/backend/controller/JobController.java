package com.workstudy.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;

import com.workstudy.backend.model.Job;
import com.workstudy.backend.model.Student;
import com.workstudy.backend.repository.JobRepository;
import com.workstudy.backend.repository.ApplicationRepository;
import com.workstudy.backend.repository.WorkHourRepository;
import com.workstudy.backend.repository.StudentRepository;
import com.workstudy.backend.service.MatchingEngine;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = {"*"})
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private WorkHourRepository workHourRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MatchingEngine matchingEngine;

    @PostMapping
    public Job addJob(@RequestBody Job job) {
        return jobRepository.save(job);
    }

    @GetMapping
    public List<Job> getJobs() {
        return jobRepository.findAll();
    }

    @GetMapping("/recommendations/{studentId}")
    public List<Job> getRecommendedJobs(@PathVariable Long studentId) {
        Student s = studentRepository.findById(studentId).orElse(null);
        List<Job> allJobs = jobRepository.findAll();
        
        if (s == null || s.getMasterResumePath() == null || s.getMasterResumePath().isEmpty()) {
            return allJobs; // Return unscored if no resume
        }

        String resumeText = matchingEngine.parsePdfToText(s.getMasterResumePath());
        
        for (Job j : allJobs) {
            MatchingEngine.MatchResult res = matchingEngine.calculateMatch(resumeText, j.getRequiredSkills());
            j.setMatchScore(res.score);
            j.setMissingSkills(res.missingSkills);
        }
        
        allJobs.sort((a, b) -> {
            int scoreA = a.getMatchScore() == null ? 0 : a.getMatchScore();
            int scoreB = b.getMatchScore() == null ? 0 : b.getMatchScore();
            return Integer.compare(scoreB, scoreA);
        });
        
        return allJobs;
    }

    @Transactional
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        System.out.println("Deleting job ID: " + id);
        workHourRepository.deleteByJobId(id);
        applicationRepository.deleteByJobId(id);
        jobRepository.deleteById(id);
    }
}

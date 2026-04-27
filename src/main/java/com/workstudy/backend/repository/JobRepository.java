package com.workstudy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workstudy.backend.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
}

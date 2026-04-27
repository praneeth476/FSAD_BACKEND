package com.workstudy.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.workstudy.backend.model.WorkHour;

public interface WorkHourRepository extends JpaRepository<WorkHour, Long> {

    List<WorkHour> findByStudentId(Long studentId);

    List<WorkHour> findByStudentIdAndJobId(Long studentId, Long jobId);
    
    @org.springframework.data.jpa.repository.Modifying
    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM WorkHour w WHERE w.student.id = :studentId")
    void deleteByStudentId(Long studentId);
    
    @org.springframework.data.jpa.repository.Modifying
    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM WorkHour w WHERE w.job.id = :jobId")
    void deleteByJobId(Long jobId);
}

package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findBySchoolClassId(Long schoolClassId);
    List<Exam> findBySchoolId(Long schoolId);
}

package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySchoolId(Long schoolId);
}

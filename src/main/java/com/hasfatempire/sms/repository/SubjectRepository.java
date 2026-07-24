package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}

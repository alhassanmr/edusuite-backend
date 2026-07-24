package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}

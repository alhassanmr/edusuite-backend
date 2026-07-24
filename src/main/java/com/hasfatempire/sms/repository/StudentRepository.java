package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByAdmissionNumber(String admissionNumber);
    List<Student> findBySchoolClassId(Long schoolClassId);
    List<Student> findByParentGuardianId(Long parentId);
    List<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String first, String last);
}

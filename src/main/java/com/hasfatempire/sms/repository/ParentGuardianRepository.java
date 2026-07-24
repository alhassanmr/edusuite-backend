package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.ParentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentGuardianRepository extends JpaRepository<ParentGuardian, Long> {
    List<ParentGuardian> findBySchoolId(Long schoolId);
}

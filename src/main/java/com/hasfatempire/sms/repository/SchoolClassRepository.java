package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findBySchoolId(Long schoolId);
}

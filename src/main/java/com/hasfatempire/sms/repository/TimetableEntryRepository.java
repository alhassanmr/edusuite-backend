package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findBySchoolClassId(Long schoolClassId);
    List<TimetableEntry> findByTeacherId(Long teacherId);
    List<TimetableEntry> findBySchoolId(Long schoolId);
}

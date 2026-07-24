package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findBySchoolClassIdAndDate(Long schoolClassId, LocalDate date);
    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);
    List<Attendance> findByDate(LocalDate date);
    long countByDateAndStatus(LocalDate date, Attendance.AttendanceStatus status);
}

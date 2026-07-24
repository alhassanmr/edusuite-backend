package com.hasfatempire.sms.service;

import com.hasfatempire.sms.dto.AttendanceMarkRequest;
import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Attendance;
import com.hasfatempire.sms.model.SchoolClass;
import com.hasfatempire.sms.model.Student;
import com.hasfatempire.sms.repository.AttendanceRepository;
import com.hasfatempire.sms.repository.SchoolClassRepository;
import com.hasfatempire.sms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;

    public List<Attendance> markBulk(AttendanceMarkRequest request, String recordedBy) {
        SchoolClass schoolClass = classRepository.findById(request.schoolClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        return request.entries().stream().map(entry -> {
            Student student = studentRepository.findById(entry.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entry.studentId()));

            Attendance attendance = attendanceRepository.findByStudentIdAndDate(entry.studentId(), request.date())
                    .orElse(Attendance.builder()
                            .student(student)
                            .schoolClass(schoolClass)
                            .date(request.date())
                            .build());

            attendance.setStatus(entry.status());
            attendance.setRemarks(entry.remarks());
            attendance.setRecordedBy(recordedBy);
            return attendanceRepository.save(attendance);
        }).toList();
    }

    public List<Attendance> byClassAndDate(Long classId, LocalDate date) {
        return attendanceRepository.findBySchoolClassIdAndDate(classId, date);
    }

    public List<Attendance> byStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    public double todayAttendancePercent() {
        LocalDate today = LocalDate.now();
        List<Attendance> records = attendanceRepository.findByDate(today);
        if (records.isEmpty()) return 0.0;
        long present = records.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
        return Math.round((present * 1000.0 / records.size())) / 10.0;
    }
}

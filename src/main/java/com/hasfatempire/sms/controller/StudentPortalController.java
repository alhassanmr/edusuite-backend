package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.*;
import com.hasfatempire.sms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student Portal — students see ONLY their own data.
 * Requires user.role = STUDENT and user.linkedStudentId set.
 */
@RestController
@RequestMapping("/api/portal/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentPortalController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ResultRepository resultRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final TimetableEntryRepository timetableRepository;
    private final NoticeRepository noticeRepository;

    private Student me(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getLinkedStudentId() == null) {
            throw new ResourceNotFoundException("No student record linked to this account. Ask the school admin to link you.");
        }
        return studentRepository.findById(user.getLinkedStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student record not found"));
    }

    /** My profile + summary */
    @GetMapping("/me")
    public Map<String, Object> profile(Authentication auth) {
        Student student = me(auth);

        List<Attendance> attendance = attendanceRepository.findByStudentId(student.getId());
        long present = attendance.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
        double attendanceRate = attendance.isEmpty() ? 0 : Math.round(present * 1000.0 / attendance.size()) / 10.0;

        Map<String, Object> profile = new HashMap<>();
        profile.put("student", student);
        profile.put("attendanceRate", attendanceRate);
        profile.put("schoolClass", student.getSchoolClass());
        return profile;
    }

    /** My attendance history */
    @GetMapping("/attendance")
    public List<Attendance> attendance(Authentication auth) {
        return attendanceRepository.findByStudentId(me(auth).getId());
    }

    /** My published results (transcript) */
    @GetMapping("/results")
    public List<Result> results(Authentication auth) {
        return resultRepository.findByStudentId(me(auth).getId()).stream()
                .filter(r -> Boolean.TRUE.equals(r.getExam().getPublished()))
                .toList();
    }

    /** My fee status */
    @GetMapping("/fees")
    public List<FeeInvoice> fees(Authentication auth) {
        return feeInvoiceRepository.findByStudentId(me(auth).getId());
    }

    /** My class timetable */
    @GetMapping("/timetable")
    public List<TimetableEntry> timetable(Authentication auth) {
        Student student = me(auth);
        if (student.getSchoolClass() == null) return List.of();
        return timetableRepository.findBySchoolClassId(student.getSchoolClass().getId());
    }

    /** Notices for students (school-scoped) */
    @GetMapping("/notices")
    public List<Notice> notices(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return noticeRepository.findBySchoolIdAndAudienceInOrderByPostedAtDesc(
                user.getSchool().getId(),
                List.of(Notice.Audience.ALL, Notice.Audience.STUDENTS));
    }
}

package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.dto.AttendanceMarkRequest;
import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.*;
import com.hasfatempire.sms.repository.*;
import com.hasfatempire.sms.service.AttendanceService;
import com.hasfatempire.sms.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Teacher Portal — teacher's daily workspace: their classes, timetable,
 * attendance marking, result entry.
 * Requires user.role = TEACHER; class data scoped via linkedTeacherId.
 */
@RestController
@RequestMapping("/api/portal/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherPortalController {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final TimetableEntryRepository timetableRepository;
    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final ExamService examService;
    private final NoticeRepository noticeRepository;

    private Teacher me(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getLinkedTeacherId() == null) {
            throw new ResourceNotFoundException("No teacher record linked to this account. Ask the school admin to link you.");
        }
        return teacherRepository.findById(user.getLinkedTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher record not found"));
    }

    /** My profile + teaching summary */
    @GetMapping("/me")
    public Map<String, Object> profile(Authentication auth) {
        Teacher teacher = me(auth);
        List<TimetableEntry> schedule = timetableRepository.findByTeacherId(teacher.getId());

        Map<String, Object> profile = new HashMap<>();
        profile.put("teacher", teacher);
        profile.put("weeklyLessons", schedule.size());
        profile.put("schedule", schedule);
        return profile;
    }

    /** My weekly timetable */
    @GetMapping("/timetable")
    public List<TimetableEntry> timetable(Authentication auth) {
        return timetableRepository.findByTeacherId(me(auth).getId());
    }

    /** Students in a class (for attendance/results entry) */
    @GetMapping("/classes/{classId}/students")
    public List<Student> classStudents(@PathVariable Long classId, Authentication auth) {
        me(auth); // verify teacher link
        return studentRepository.findBySchoolClassId(classId);
    }

    /** Mark attendance for a class */
    @PostMapping("/attendance/mark")
    public List<Attendance> markAttendance(@RequestBody AttendanceMarkRequest request, Authentication auth) {
        me(auth);
        return attendanceService.markBulk(request, auth.getName());
    }

    /** Record a student's exam result */
    @PostMapping("/exams/{examId}/results/{studentId}")
    public Result recordResult(@PathVariable Long examId, @PathVariable Long studentId,
                                @RequestBody Result result, Authentication auth) {
        me(auth);
        return examService.recordResultInternal(examId, studentId, result);
    }

    /** Notices for teachers (school-scoped) */
    @GetMapping("/notices")
    public List<Notice> notices(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return noticeRepository.findBySchoolIdAndAudienceInOrderByPostedAtDesc(
                user.getSchool().getId(),
                List.of(Notice.Audience.ALL, Notice.Audience.TEACHERS));
    }
}

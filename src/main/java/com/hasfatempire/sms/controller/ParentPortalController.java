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
 * Parent Portal — parents see ONLY their own children's data.
 * Requires user.role = PARENT and user.linkedParentId set.
 */
@RestController
@RequestMapping("/api/portal/parent")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
public class ParentPortalController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final ResultRepository resultRepository;
    private final NoticeRepository noticeRepository;

    private Long parentIdOf(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getLinkedParentId() == null) {
            throw new ResourceNotFoundException("No parent record linked to this account. Ask the school admin to link you.");
        }
        return user.getLinkedParentId();
    }

    private Student ownedChild(Long studentId, Authentication auth) {
        Long parentId = parentIdOf(auth);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (student.getParentGuardian() == null || !student.getParentGuardian().getId().equals(parentId)) {
            throw new ResourceNotFoundException("Student not found"); // don't leak existence
        }
        return student;
    }

    /** My children */
    @GetMapping("/children")
    public List<Student> children(Authentication auth) {
        return studentRepository.findByParentGuardianId(parentIdOf(auth));
    }

    /** Child overview: attendance summary, fees, latest results */
    @GetMapping("/children/{studentId}/overview")
    public Map<String, Object> childOverview(@PathVariable Long studentId, Authentication auth) {
        Student student = ownedChild(studentId, auth);

        List<Attendance> attendance = attendanceRepository.findByStudentId(studentId);
        long present = attendance.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
        double attendanceRate = attendance.isEmpty() ? 0 : Math.round(present * 1000.0 / attendance.size()) / 10.0;

        List<FeeInvoice> invoices = feeInvoiceRepository.findByStudentId(studentId);
        var outstanding = invoices.stream()
                .map(inv -> inv.getAmountDue().subtract(inv.getAmountPaid()))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Only published exam results are visible to parents
        List<Result> results = resultRepository.findByStudentId(studentId).stream()
                .filter(r -> Boolean.TRUE.equals(r.getExam().getPublished()))
                .toList();

        Map<String, Object> overview = new HashMap<>();
        overview.put("student", student);
        overview.put("attendanceRate", attendanceRate);
        overview.put("totalAttendanceRecords", attendance.size());
        overview.put("feesOutstanding", outstanding);
        overview.put("invoices", invoices);
        overview.put("publishedResults", results);
        return overview;
    }

    /** Child attendance history */
    @GetMapping("/children/{studentId}/attendance")
    public List<Attendance> childAttendance(@PathVariable Long studentId, Authentication auth) {
        ownedChild(studentId, auth);
        return attendanceRepository.findByStudentId(studentId);
    }

    /** Child fee invoices */
    @GetMapping("/children/{studentId}/fees")
    public List<FeeInvoice> childFees(@PathVariable Long studentId, Authentication auth) {
        ownedChild(studentId, auth);
        return feeInvoiceRepository.findByStudentId(studentId);
    }

    /** Child published results only */
    @GetMapping("/children/{studentId}/results")
    public List<Result> childResults(@PathVariable Long studentId, Authentication auth) {
        ownedChild(studentId, auth);
        return resultRepository.findByStudentId(studentId).stream()
                .filter(r -> Boolean.TRUE.equals(r.getExam().getPublished()))
                .toList();
    }

    /** Notices for parents (school-scoped) */
    @GetMapping("/notices")
    public List<Notice> notices(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return noticeRepository.findBySchoolIdAndAudienceInOrderByPostedAtDesc(
                user.getSchool().getId(),
                List.of(Notice.Audience.ALL, Notice.Audience.PARENTS));
    }
}

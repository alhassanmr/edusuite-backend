package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Exam;
import com.hasfatempire.sms.model.Result;
import com.hasfatempire.sms.model.Student;
import com.hasfatempire.sms.notification.NotificationService;
import com.hasfatempire.sms.repository.ExamRepository;
import com.hasfatempire.sms.repository.ResultRepository;
import com.hasfatempire.sms.repository.StudentRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ResultRepository resultRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final TenantContext tenantContext;

    public List<Exam> findAll(Authentication auth) {
        return examRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    public Exam findById(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        if (exam.getSchool() == null || !exam.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Exam not found: " + id);
        }
        return exam;
    }

    public List<Exam> byClass(Long classId, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        return examRepository.findBySchoolClassId(classId).stream()
                .filter(e -> e.getSchool() != null && e.getSchool().getId().equals(schoolId))
                .toList();
    }

    public Exam create(Exam exam, Authentication auth) {
        exam.setSchool(tenantContext.getCurrentSchool(auth));
        return examRepository.save(exam);
    }

    public Exam publish(Long id, Authentication auth) {
        Exam exam = findById(id, auth);
        exam.setPublished(true);
        Exam saved = examRepository.save(exam);
        notifyResultsPublished(saved);
        return saved;
    }

    private void notifyResultsPublished(Exam exam) {
        List<Result> results = resultRepository.findByExamId(exam.getId());
        for (Result result : results) {
            Student student = result.getStudent();
            if (student == null) continue;
            String message = String.format(
                    "%s: Results for %s are now published for %s %s. Score: %s (%s). Log in to the portal for details.",
                    exam.getSchool() != null ? exam.getSchool().getName() : "School",
                    exam.getName(),
                    student.getFirstName(), student.getLastName(),
                    result.getScore(), result.getGrade());
            if (student.getParentGuardian() != null) {
                notificationService.notifyBoth(exam.getSchool(),
                        student.getParentGuardian().getPhone(),
                        student.getParentGuardian().getEmail(),
                        "Exam results published — " + exam.getName(), message);
            }
        }
    }

    /** Tenant-checked result recording used by admin controller. */
    public Result recordResult(Long examId, Long studentId, Result incoming, Authentication auth) {
        findById(examId, auth); // ownership check on exam
        return recordResultInternal(examId, studentId, incoming);
    }

    /** Used by teacher portal (teacher link already verified there). */
    public Result recordResultInternal(Long examId, Long studentId, Result incoming) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        Result result = resultRepository.findByExamIdAndStudentId(examId, studentId)
                .orElse(Result.builder().exam(exam).student(student).build());
        result.setScore(incoming.getScore());
        result.setGrade(gradeFor(incoming.getScore(), exam.getMaxScore()));
        result.setRemarks(incoming.getRemarks());
        return resultRepository.save(result);
    }

    public List<Result> resultsForExam(Long examId, Authentication auth) {
        findById(examId, auth);
        return resultRepository.findByExamId(examId);
    }

    public List<Result> resultsForStudent(Long studentId) {
        return resultRepository.findByStudentId(studentId);
    }

    private String gradeFor(Double score, Double maxScore) {
        if (score == null || maxScore == null || maxScore == 0) return "N/A";
        double pct = (score / maxScore) * 100;
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        return "F";
    }
}

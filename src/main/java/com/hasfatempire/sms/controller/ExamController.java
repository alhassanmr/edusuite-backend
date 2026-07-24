package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Exam;
import com.hasfatempire.sms.model.Result;
import com.hasfatempire.sms.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public List<Exam> all(@RequestParam(required = false) Long classId, Authentication auth) {
        return classId != null ? examService.byClass(classId, auth) : examService.findAll(auth);
    }

    @GetMapping("/{id}")
    public Exam byId(@PathVariable Long id, Authentication auth) { return examService.findById(id, auth); }

    @PostMapping
    public ResponseEntity<Exam> create(@Valid @RequestBody Exam exam, Authentication auth) {
        return ResponseEntity.ok(examService.create(exam, auth));
    }

    @PostMapping("/{id}/publish")
    public Exam publish(@PathVariable Long id, Authentication auth) { return examService.publish(id, auth); }

    @PostMapping("/{examId}/results/{studentId}")
    public Result recordResult(@PathVariable Long examId, @PathVariable Long studentId,
                                @RequestBody Result result, Authentication auth) {
        return examService.recordResult(examId, studentId, result, auth);
    }

    @GetMapping("/{examId}/results")
    public List<Result> resultsForExam(@PathVariable Long examId, Authentication auth) {
        return examService.resultsForExam(examId, auth);
    }

    @GetMapping("/results/student/{studentId}")
    public List<Result> resultsForStudent(@PathVariable Long studentId) {
        return examService.resultsForStudent(studentId);
    }
}

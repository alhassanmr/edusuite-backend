package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Exam;
import com.hasfatempire.sms.model.Result;
import com.hasfatempire.sms.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public List<Exam> all(@RequestParam(required = false) Long classId) {
        return classId != null ? examService.byClass(classId) : examService.findAll();
    }

    @GetMapping("/{id}")
    public Exam byId(@PathVariable Long id) { return examService.findById(id); }

    @PostMapping
    public ResponseEntity<Exam> create(@Valid @RequestBody Exam exam) {
        return ResponseEntity.ok(examService.create(exam));
    }

    @PostMapping("/{id}/publish")
    public Exam publish(@PathVariable Long id) { return examService.publish(id); }

    @PostMapping("/{examId}/results/{studentId}")
    public Result recordResult(@PathVariable Long examId, @PathVariable Long studentId, @RequestBody Result result) {
        return examService.recordResult(examId, studentId, result);
    }

    @GetMapping("/{examId}/results")
    public List<Result> resultsForExam(@PathVariable Long examId) {
        return examService.resultsForExam(examId);
    }

    @GetMapping("/results/student/{studentId}")
    public List<Result> resultsForStudent(@PathVariable Long studentId) {
        return examService.resultsForStudent(studentId);
    }
}

package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Student;
import com.hasfatempire.sms.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<Student> all(@RequestParam(required = false) Long classId,
                              @RequestParam(required = false) String q,
                              Authentication auth) {
        if (classId != null) return studentService.findByClass(classId, auth);
        if (q != null && !q.isBlank()) return studentService.search(q, auth);
        return studentService.findAll(auth);
    }

    @GetMapping("/{id}")
    public Student byId(@PathVariable Long id, Authentication auth) {
        return studentService.findById(id, auth);
    }

    @PostMapping
    public ResponseEntity<Student> create(@Valid @RequestBody Student student, Authentication auth) {
        return ResponseEntity.ok(studentService.create(student, auth));
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @Valid @RequestBody Student student, Authentication auth) {
        return studentService.update(id, student, auth);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        studentService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}

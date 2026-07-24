package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Student;
import com.hasfatempire.sms.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<Student> all(@RequestParam(required = false) Long classId,
                              @RequestParam(required = false) String q) {
        if (classId != null) return studentService.findByClass(classId);
        if (q != null && !q.isBlank()) return studentService.search(q);
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public Student byId(@PathVariable Long id) { return studentService.findById(id); }

    @PostMapping
    public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
        return ResponseEntity.ok(studentService.create(student));
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @Valid @RequestBody Student student) {
        return studentService.update(id, student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

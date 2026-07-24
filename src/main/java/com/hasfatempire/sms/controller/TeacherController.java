package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Teacher;
import com.hasfatempire.sms.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public List<Teacher> all() { return teacherService.findAll(); }

    @GetMapping("/{id}")
    public Teacher byId(@PathVariable Long id) { return teacherService.findById(id); }

    @PostMapping
    public ResponseEntity<Teacher> create(@Valid @RequestBody Teacher teacher) {
        return ResponseEntity.ok(teacherService.create(teacher));
    }

    @PutMapping("/{id}")
    public Teacher update(@PathVariable Long id, @Valid @RequestBody Teacher teacher) {
        return teacherService.update(id, teacher);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

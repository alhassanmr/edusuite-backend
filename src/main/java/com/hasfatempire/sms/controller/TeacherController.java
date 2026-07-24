package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Teacher;
import com.hasfatempire.sms.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public List<Teacher> all(Authentication auth) { return teacherService.findAll(auth); }

    @GetMapping("/{id}")
    public Teacher byId(@PathVariable Long id, Authentication auth) { return teacherService.findById(id, auth); }

    @PostMapping
    public ResponseEntity<Teacher> create(@Valid @RequestBody Teacher teacher, Authentication auth) {
        return ResponseEntity.ok(teacherService.create(teacher, auth));
    }

    @PutMapping("/{id}")
    public Teacher update(@PathVariable Long id, @Valid @RequestBody Teacher teacher, Authentication auth) {
        return teacherService.update(id, teacher, auth);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        teacherService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}

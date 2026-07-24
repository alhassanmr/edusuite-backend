package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Subject;
import com.hasfatempire.sms.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public List<Subject> all(Authentication auth) { return subjectService.findAll(auth); }

    @PostMapping
    public ResponseEntity<Subject> create(@Valid @RequestBody Subject subject, Authentication auth) {
        return ResponseEntity.ok(subjectService.create(subject, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        subjectService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}

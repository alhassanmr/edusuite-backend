package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.SchoolClass;
import com.hasfatempire.sms.service.SchoolClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService classService;

    @GetMapping
    public List<SchoolClass> all(Authentication auth) { return classService.findAll(auth); }

    @GetMapping("/{id}")
    public SchoolClass byId(@PathVariable Long id, Authentication auth) { return classService.findById(id, auth); }

    @PostMapping
    public ResponseEntity<SchoolClass> create(@Valid @RequestBody SchoolClass schoolClass, Authentication auth) {
        return ResponseEntity.ok(classService.create(schoolClass, auth));
    }

    @PutMapping("/{id}")
    public SchoolClass update(@PathVariable Long id, @Valid @RequestBody SchoolClass schoolClass, Authentication auth) {
        return classService.update(id, schoolClass, auth);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        classService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}

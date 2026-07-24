package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.SchoolClass;
import com.hasfatempire.sms.service.SchoolClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService classService;

    @GetMapping
    public List<SchoolClass> all() { return classService.findAll(); }

    @GetMapping("/{id}")
    public SchoolClass byId(@PathVariable Long id) { return classService.findById(id); }

    @PostMapping
    public ResponseEntity<SchoolClass> create(@Valid @RequestBody SchoolClass schoolClass) {
        return ResponseEntity.ok(classService.create(schoolClass));
    }

    @PutMapping("/{id}")
    public SchoolClass update(@PathVariable Long id, @Valid @RequestBody SchoolClass schoolClass) {
        return classService.update(id, schoolClass);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

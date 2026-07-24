package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.ParentGuardian;
import com.hasfatempire.sms.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping
    public List<ParentGuardian> all() { return parentService.findAll(); }

    @GetMapping("/{id}")
    public ParentGuardian byId(@PathVariable Long id) { return parentService.findById(id); }

    @GetMapping("/{id}/children")
    public Object children(@PathVariable Long id) { return parentService.children(id); }

    @PostMapping
    public ResponseEntity<ParentGuardian> create(@Valid @RequestBody ParentGuardian parent) {
        return ResponseEntity.ok(parentService.create(parent));
    }

    @PutMapping("/{id}")
    public ParentGuardian update(@PathVariable Long id, @Valid @RequestBody ParentGuardian parent) {
        return parentService.update(id, parent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        parentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
